package jaspr.weka.core;

import jaspr.weka.classifiers.meta.ParamSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.classifiers.SingleClassifierEnhancer;

import java.util.Arrays;

/**
 * Created by phil on 31/03/16.
 */
public class LearnerFactory {

    //Gives chance to have options optimized
    public static Classifier makeLearner(String learnerStr, String optionSearchRange) throws Exception {
        Classifier learner = null;
        if (optionSearchRange != null) {

            ParamSelection ps = new ParamSelection();
            ps.setDebug(true);
//            System.out.println(ArrayUtils.arrToString(DMParameters.splitForBrackets(learnerStr)));
            Classifier base = LearnerFactory.makeLearner(splitForBrackets(learnerStr), false);

            ps.setClassifier(base);
            ps.setEvaluationMetric(ParamSelection.EvaluationMetric.AUC);
            ps.setNumFolds(10);
            ps.setCVParameters(optionSearchRange.split(", "));

            learner = AbstractClassifier.makeCopy(ps);
        } else {
//            System.out.println(DMParameters.splitForBrackets(learnerStr));
            learner = LearnerFactory.makeLearner(splitForBrackets(learnerStr), false);
        }
        return learner;
    }

    //Just makes it.
    public static Classifier makeLearner(String[] l, boolean debug) throws Exception {
        String clss = removeOptions(l[0]);
        System.out.println(clss);
        String[] options = getOptions(l[0]);
        for (String o : options) {
            System.out.println(o);
        }
        Classifier ret = AbstractClassifier.forName(clss, options);
        if (l.length > 1) {
            if (ret instanceof SingleClassifierEnhancer) {
                String[] next = Arrays.copyOfRange(l, 1, l.length);
                Classifier base = makeLearner(next, debug);
                ((SingleClassifierEnhancer) ret).setClassifier(base);
                ((SingleClassifierEnhancer) ret).setDebug(debug);
            } else if (ret instanceof MultipleClassifiersCombiner) {
                throw new UnsupportedOperationException();
            }
        }
        System.out.println("Making learner: " + l[0]);
        return ret;
    }


    public static String[] splitForBrackets(String arg) {
        int openBrace = arg.indexOf('(');
        int closeBrace = arg.lastIndexOf(')');
        if (openBrace == -1 || closeBrace == -1) {
            return new String[]{arg};
        }

        String beforeBrace = arg.substring(0, openBrace).trim();
        String afterBrace = arg.substring(closeBrace + 1).trim();
        String[] betweenBrace = splitForBrackets(arg.substring(openBrace + 1, closeBrace).trim());

        int retlen = betweenBrace.length + (beforeBrace.isEmpty() ? 0 : 1) + (afterBrace.isEmpty() ? 0 : 1);
        String[] ret = new String[retlen];
        if (!beforeBrace.isEmpty()) ret[0] = beforeBrace;
        System.arraycopy(betweenBrace, 0, ret, beforeBrace.isEmpty() ? 0 : 1, betweenBrace.length);
        if (!afterBrace.isEmpty()) ret[ret.length - 1] = afterBrace;

        return ret;
    }

    private static String[] getOptions(String learnerString) {
        String[] temp = learnerString.split("&");
        return Arrays.copyOfRange(temp, 1, temp.length);
    }

    private static String removeOptions(String learnerString) {
        return learnerString.split("&")[0].trim();
    }

}
