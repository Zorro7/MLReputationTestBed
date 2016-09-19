package jaspr.weka.classifiers.meta;

import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.supervised.attribute.Discretize;

import java.io.Serializable;

/**
 * Created by phil on 16/09/2016.
 */
public class DiscretizeClassifier extends SingleClassifierEnhancer implements Classifier, Serializable {

    public DiscretizeClassifier() {}

    private FilteredClassifier filteredClassifier;

    @Override
    public void setClassifier(Classifier base) {
        this.m_Classifier = base;
    }

    @Override
    public void buildClassifier(Instances instances) throws Exception {
        filteredClassifier = new FilteredClassifier();
        Discretize sd = new Discretize();
        sd.setAttributeIndices("first-last");
        filteredClassifier.setFilter(sd);

        filteredClassifier.buildClassifier(instances);
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        return filteredClassifier.classifyInstance(instance);
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        return filteredClassifier.distributionForInstance(instance);
    }
}
