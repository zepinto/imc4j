package pt.lsts.imc4j.util;

import java.util.Objects;
import java.util.function.Function;

public class PointIndex {
    private int min;
    private int max;
    private int curIdx = min;

    private Function<Integer, Boolean> customTest = (v) -> true;

    public PointIndex(int min, int max) {
        if (min > max || max < min) {
            throw new IndexOutOfBoundsException(String.format(
                    "Min and max assert does not compute: %d <= %d !!!",
                    min, max));
        }

        this.min = min;
        this.max = max;
        curIdx = this.min;
    }

    public PointIndex(int max) {
        this(0, max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int index() {
        return curIdx;
    }

    public int setIndex(int val) {
        curIdx = val > max ? max : (val < min ? min : val);
        return curIdx;
    }

    public int getAndIncrementIndex() {
        return setIndex(curIdx++);
    }

    public int incrementAndGetIndex() {
        return setIndex(++curIdx);
    }

    public int resetIndex() {
        return setIndex(min);
    }

    public boolean isMinIndex() {
        return curIdx == min;
    }

    public boolean isMaxIndex() {
        return curIdx == max;
    }

    public boolean isValIndex(int val) {
        return curIdx == val;
    }

    public void setCustomTest(Function<Integer, Boolean> testFunction) {
        customTest = testFunction;
    }

    public boolean customTestCall() {
        return customTest.apply(curIdx);
    };

    public boolean test(Function<Integer, Boolean> testFunction) {
        return testFunction.apply(curIdx);
    }

    @Override
    public String toString() {
        return "PointIndex{" +
                "index=" + curIdx +
                " from [" + min + "; " + max + "]" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PointIndex that = (PointIndex) o;
        return min == that.min && max == that.max && curIdx == that.curIdx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, curIdx);
    }
}
