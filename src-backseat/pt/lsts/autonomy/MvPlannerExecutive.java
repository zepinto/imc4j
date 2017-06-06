package pt.lsts.autonomy;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalPlan;
import pt.lsts.imc4j.util.FormatConversion;
import pt.lsts.imc4j.util.PojoConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

/**
 * Created by tsm on 31/05/17.
 */
public class MvPlannerExecutive extends MissionExecutive {
    @Parameter
    public String host = "127.0.0.1";

    @Parameter
    public int port = 6003;

    @Parameter
    public int systemId = -1;

    /** Current plan being executed **/
    private TemporalPlan currPlan = null;

    /** Actions for this system to execute  **/
    private final Queue<TemporalAction> toExecute = new PriorityQueue<>(Comparator.comparingDouble(o -> o.start_time));

    /** All finished or ongoing tasks **/
    private final Stack<TemporalAction> handledActions = new Stack<>();

    /** Current being executed  **/
    private TemporalAction currAction = null;

    public MvPlannerExecutive() throws ParseException {
        this.state = this::idle;
    }

    @Consume
    protected void on(Announce msg) {
        if(systemId == -1 && msg.sys_type == SystemType.UUV && msg.sys_name.contains("lauv-seacon-1")) {
            systemId = msg.src;
            log("Running in " + msg.sys_name + " with id " + systemId);
            return;
        }

        // if ccu then disseminate handledActions
        if(msg.sys_type != SystemType.CCU || handledActions.isEmpty() || currAction != null)
            return;

        this.state = this::communicate;
    }

    @Consume
    protected void on(PlanControlState msg) {
        if(currAction == null)
            return;

        if(msg.src != currAction.system_id || msg.plan_id != currAction.action_id)
            return;

        boolean success = false;
        boolean failed = false;
        if (msg.state == PlanControlState.STATE.PCS_READY || msg.state == PlanControlState.STATE.PCS_BLOCKED) {
            success = msg.last_outcome == PlanControlState.LAST_OUTCOME.LPO_SUCCESS;
            failed = !success;
        }
        else if (msg.state == PlanControlState.STATE.PCS_READY)
            if (msg.plan_progress == 100)
                success = true;

        if(success) {
            log("Finished " + currAction.action_id + " with success");
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FINISHED;
            currAction = null;

            this.state = this::idle;
        }
        else if(failed) {
            log("Failed " + currAction.action_id);
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FAILED;
            currAction = null;

            this.state = this::idle;
        }
    }

    @Consume
    protected void on(TemporalPlan msg) {
        log("Got a new plan with " + msg.actions.size() + " actions");
        currPlan = msg;

        // collect tasks for this system
        // sort them by start time
        currPlan.actions.stream()
                .filter(a -> a.system_id == systemId)
                .forEach(a -> toExecute.add(a));
    }

    /**
     * Wait for TemporalPlan and then,
     * for appropriate time to run a TemporalAction
     * */
    private State idle() {
        // nothing to do
        if(currPlan == null || currAction != null || toExecute.isEmpty())
            return this::idle;

        if(System.currentTimeMillis() >=  currPlan.actions.get(0).start_time)
            return this::exec;

        return this::idle;
    }

    /**
     * Execute a TemporalAction
     * */
    private State exec() {
        if(currAction != null)
            return this::exec;

        // allocate task
        currAction = toExecute.poll();
        TemporalAction action = new TemporalAction();
        action.action_id = currAction.action_id;
        action.system_id = currAction.system_id;
        action.status = TemporalAction.STATUS.ASTAT_SCHEDULED;

        handledActions.push(action);
        dispatch(currAction.action);

        log("Executing action with id " + currAction.action.plan_id);
        return this::exec;
    }

    /**
     * Communicate with a Neptus console the current status
     * of the TemporalPlan
     * */
    private synchronized State communicate() {
        log("Communicating");
        handledActions.forEach(a -> dispatch(a));

        return this::idle;
    }

    private void log(String msg) {
        System.out.println("[MvPlannerExecutive]: " + msg + "\n");
    }

    private class TemporalActionComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            return Double.compare(((TemporalAction) o1).start_time,
                    ((TemporalAction) o2).start_time);
        }
    }

    public static void main(String[] args) throws Exception {
        MvPlannerExecutive exec = PojoConfig.create(MvPlannerExecutive.class, args);
        exec.connect(exec.host, exec.port);
        exec.join();
    }
}
