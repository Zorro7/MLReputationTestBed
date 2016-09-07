/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jaspr.weka.utilities;

import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;

/**
 * @author phil
 */
public class EvaluatingUtils {

    public static double weightedAUC(ArrayList<Prediction> preds) {
        int m_NumClasses = ((NominalPrediction) preds.get(0)).distribution().length;
        double[] classCounts = new double[m_NumClasses];
        double classCountSum = 0;

        for (Prediction p : preds) {
            classCounts[(int) p.actual()] += p.weight();
            classCountSum += p.weight();
        }

        double aucTotal = 0;
        for (int i = 0; i < m_NumClasses; i++) {
            double temp = areaUnderROC(preds, i);
            if (!Utils.isMissingValue(temp)) {
                aucTotal += (temp * classCounts[i]);
            }
        }

        return aucTotal / classCountSum;
    }

    public static double areaUnderROC(ArrayList<Prediction> preds, int classIndex) {
        ThresholdCurve tc = new ThresholdCurve();
        Instances result = tc.getCurve(preds, classIndex);
        return ThresholdCurve.getROCArea(result);
    }

}

