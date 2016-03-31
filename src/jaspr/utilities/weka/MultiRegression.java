package jaspr.utilities.weka;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;

/**
 * Created by phil on 05/11/15.
 */
public class MultiRegression implements Classifier, Serializable {

    public MultiRegression() {}

    private Classifier base = new LinearRegression();

    private String[] splitValues;
    private Classifier[] models;
    private Instances[] trains;
    private int splitAttIndex = -1;

    public void setBase(Classifier base) {
        this.base = base;
    }
    public Classifier getBase() { return this.base; }
    public void setSplitAttIndex(int index) {
        this.splitAttIndex = index;
    }

    @Override
    public void buildClassifier(Instances instances) throws Exception {
        if (splitAttIndex < 0) {
            splitAttIndex = instances.numAttributes()-2-splitAttIndex;
        }
        if (!instances.attribute(splitAttIndex).isNominal()) {
            throw new Exception("Non-nominal attribute cannot be split on.");
        }
        if (splitAttIndex == instances.classIndex()) {
            throw new Exception("Cannot split on class attribute.");
        }


        Instances data = new Instances(instances);
        models = AbstractClassifier.makeCopies(base, data.attribute(splitAttIndex).numValues());
        splitValues = new String[models.length];
        trains = new Instances[models.length];

        for (int i=0;i<data.attribute(splitAttIndex).numValues();i++) {
            Instances splitdata = new Instances(instances, 0, 0);
            for (Instance inst: data) {
                if ((int)inst.value(splitAttIndex) == i) {
                    splitdata.add(inst);
                }
            }
            splitdata.deleteAttributeAt(splitAttIndex);

            if (splitdata.numInstances() > 0) {
                models[i].buildClassifier(splitdata);
                splitValues[i] = data.attribute(splitAttIndex).value(i);
                trains[i] = splitdata;
            } else {
                models[i] = null;
                trains[i] = null;
            }
//            System.out.print(trains[i].numInstances()+" ");
        }
//        System.out.println(this);
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        Instance copy = new DenseInstance(instance);
        copy.deleteAttributeAt(splitAttIndex);
        copy.setDataset(trains[(int)instance.value(splitAttIndex)]);
        if (models[(int)instance.value(splitAttIndex)] != null) {
//            System.out.println(trains[(int)instance.value(splitAttIndex)]);
//            try {
                return models[(int) instance.value(splitAttIndex)].classifyInstance(copy);
//            } catch(Exception ex) {
//                return -1;
//            }
        } else {
            return -1;
        }
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        Instance copy = new DenseInstance(instance);
        copy.deleteAttributeAt(splitAttIndex);
        return models[(int)instance.value(splitAttIndex)].distributionForInstance(copy);
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int i=0;i<models.length;i++) {
            ret.append(i).append(" ").append(splitValues[i]).append(":::\n")
                    .append(models[i].toString()).append("\n");
        }
        return ret.toString();
    }
}
