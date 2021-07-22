/*
 * Copyright (c) 2004-2021 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: Paulo Dias
 * 11/09/2011
 */
package pt.lsts.imc4j.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pdias
 *
 */
public class ManeuversUtil {
    
    public static final int X = 0, Y = 1, Z = 2, T = 3;

    private ManeuversUtil() {
    }
    
    /**
     * @param width
     * @param hstep
     * @param alternationPercent
     * @param curvOff
     * @param squareCurve
     * @param bearingRad
     * @return
     */
    public static List<double[]> calcRIPatternPoints(double width, double hstep,
        double alternationPercent, double curvOff, boolean squareCurve, double bearingRad) {
        
        List<double[]> newPoints = new ArrayList<double[]>();
        
        double length = width;
        double[] pointBaseB = {-length/2., -width/2., 0, -1};
        double[] res = AngleUtils.rotate(bearingRad, pointBaseB[X], pointBaseB[Y], false);
        double[] pointBase1 = new double[] {res[X], res[Y], 0, -1};
        res = AngleUtils.rotate(bearingRad+Math.toRadians(-60), pointBaseB[X], pointBaseB[Y], false);
        double[] pointBase2 = {res[X], res[Y], 0, -1};
        res = AngleUtils.rotate(bearingRad+Math.toRadians(-120), pointBaseB[X], pointBaseB[Y], false);
        double[] pointBase3 = {res[X], res[Y], 0, -1};
        
        List<double[]> points1 = calcRowsPoints(width, width, hstep, 2-alternationPercent, curvOff,
                squareCurve, bearingRad, 0);
        for (double[] pt : points1) {
            pt[X] += pointBase1[X];
            pt[Y] += pointBase1[Y];
        }

        List<double[]> points2 = calcRowsPoints(width, width, hstep, 2-alternationPercent, curvOff,
                squareCurve, bearingRad + Math.toRadians(-60), 0);
        for (double[] pt : points2) {
            pt[X] += pointBase2[X];
            pt[Y] += pointBase2[Y];
        }

        List<double[]> points3 = calcRowsPoints(width, width, hstep, 2-alternationPercent, curvOff,
                squareCurve, bearingRad + Math.toRadians(-120), 0);
        for (double[] pt : points3) {
            pt[X] += pointBase3[X];
            pt[Y] += pointBase3[Y];
        }

        newPoints.addAll(points1);
        newPoints.addAll(points2);
        newPoints.addAll(points3);
        return newPoints;
    }
    
    
    /**
     * @param width
     * @param hstep
     * @param curvOff
     * @param squareCurve
     * @param bearingRad
     * @return
     */
    public static List<double[]> calcCrossHatchPatternPoints(double width, double hstep,
            double curvOff, boolean squareCurve, double bearingRad) {
        
        List<double[]> newPoints = new ArrayList<double[]>();
        
        double length = width;
        double[] pointBase1 = {-length/2., -width/2., 0, -1};
        double[] pointBase2 = {-length/2., width/2., 0, -1};
        double[] res = AngleUtils.rotate(bearingRad, pointBase1[X], pointBase1[Y], false);
        pointBase1 = new double[] {res[X], res[Y], 0, -1};
        res = AngleUtils.rotate(bearingRad, pointBase2[X], pointBase2[Y], false);
        pointBase2 = new double[] {res[X], res[Y], 0, -1};

        List<double[]> points1 = calcRowsPoints(width, width, hstep, 1, curvOff,
                squareCurve, bearingRad, 0);
        for (double[] pt : points1) {
            pt[X] += pointBase1[X];
            pt[Y] += pointBase1[Y];
        }

        List<double[]> points2 = calcRowsPoints(width, width, hstep, 1, curvOff,
                squareCurve, bearingRad + Math.toRadians(-90), 0);
        for (double[] pt : points2) {
            pt[X] += pointBase2[X];
            pt[Y] += pointBase2[Y];
        }

        newPoints.addAll(points1);
        newPoints.addAll(points2);
        return newPoints;
    }

    
    /**
     * @param width
     * @param length
     * @param hstep
     * @param alternationPercent
     * @param curvOff
     * @param squareCurve
     * @param bearingRad
     * @param crossAngleRadians
     * @return
     */
    public static List<double[]> calcRowsPoints(double width, double length, double hstep,
            double alternationPercent, double curvOff, boolean squareCurve, double bearingRad,
            double crossAngleRadians) {
        return calcRowsPoints(width, length, hstep, alternationPercent, curvOff, squareCurve,
                bearingRad, crossAngleRadians, false);
    }
    
    /**
     * @param width
     * @param length
     * @param hstep
     * @param alternationPercent
     * @param curvOff
     * @param squareCurve
     * @param bearingRad
     * @param crossAngleRadians
     * @param invertY
     * @return
     */
    public static List<double[]> calcRowsPoints(double width, double length, double hstep,
            double alternationPercent, double curvOff, boolean squareCurve, double bearingRad,
            double crossAngleRadians, boolean invertY) {
        width = Math.abs(width);
        length = Math.abs(length);
        hstep = Math.abs(hstep);
        hstep = hstep == 0 ? 0.1 : hstep;
        
        boolean direction = true;
        List<double[]> newPoints = new ArrayList<double[]>();
        double[] point = {-curvOff, 0, 0, -1};
        newPoints.add(point);
        
        double x2;
        for (double y = 0; y <= width; y += hstep) {
            if (direction)
                x2 = length + curvOff;
            else
                x2 = -curvOff;
            direction = !direction;

            double hstepDelta = 0;
            if (direction)
                hstepDelta = hstep * (1 - alternationPercent);
            point = new double[] { x2, y - hstepDelta, 0, -1 };
            newPoints.add(point);

            if (y + hstep <= width) {
                double hstepAlt = hstep;
                if (!direction)
                    hstepAlt = hstep * alternationPercent;
                point = new double[] { x2 + (squareCurve ? 0 : 1) * (direction ? curvOff : -curvOff), y + hstepAlt, 0, -1 };
                newPoints.add(point);
            }
        }
        for (double[] pt : newPoints) {
            double[] res = AngleUtils.rotate(-crossAngleRadians, pt[X], 0, false);
            pt[X] = res[0];
            pt[Y] = pt[Y] + res[1];
            if (invertY)
                pt[Y] = -pt[Y];
            res = AngleUtils.rotate(bearingRad + (!invertY ? -1 : 1) * -crossAngleRadians, pt[X], pt[Y], false);
            pt[X] = res[0];
            pt[Y] = res[1];
        }
        return newPoints;
    }

    public static List<double[]> calcExpansiveSquarePatternPointsMaxBox(
            double width, double hstep, double bearingRad, boolean invertY) {
        width = Math.abs(width);
        hstep = Math.abs(hstep);
        hstep = hstep == 0 ? 0.1 : hstep;

        List<double[]> newPoints = new ArrayList<double[]>();
        
        final short left = 0, up = 1, right = 2, down = 3;
        
        double[] point;;
        
        double x = 0;
        double y = 0;
        short stepDir = left;
        int stepX = 1;
        int stepY = 1;
        do {
            point = new double[] { x, y, 0, -1 };
            newPoints.add(point);

            switch (stepDir) {
                case left:
                    x += hstep * stepX;
                    // y = y;
                    stepX++;
                    break;
                case up:
                    // x = x;
                    y += hstep * stepY;
                    stepY++;
                    break;
                case right:
                    x -= hstep * stepX;
                    // y = y;
                    stepX++;
                    break;
                case down:
                    // x = x;;
                    y -= hstep * stepY;
                    stepY++;
                    break;
                default:
                    throw new RuntimeException("Something went wrong!!");
            }
            stepDir = (short) (++stepDir % 4);
        } while (Math.abs(x) <= width / 2 || Math.abs(y) <= width / 2);

        double[] le = newPoints.get(newPoints.size() - 1);
        le[0] = Math.signum(le[0]) * Math.min(Math.abs(le[0]), width / 2);
        le[1] = Math.signum(le[1]) * Math.min(Math.abs(le[1]), width / 2);
        
        for (double[] pt : newPoints) {
            double[] res = AngleUtils.rotate(0, pt[X], 0, false);
            pt[X] = res[0];
            pt[Y] = pt[Y] + res[1];
            if (invertY)
                pt[Y] = -pt[Y];
            res = AngleUtils.rotate(bearingRad, pt[X], pt[Y], false);
            pt[X] = res[0];
            pt[Y] = res[1];
        }

        return newPoints;
    }
}
