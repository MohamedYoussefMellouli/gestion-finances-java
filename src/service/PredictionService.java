package service;

import model.Transaction;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;

import java.util.ArrayList;
import java.util.List;

public class PredictionService {

    private Classifier modele;
    private Instances datasetStructure;

    public PredictionService(List<Transaction> transactions) throws Exception {
        // 🔹 Définir les attributs
        ArrayList<Attribute> attributes = new ArrayList<>();
        // Nominal mais on convertira en binaire plus tard
        List<String> categories = new ArrayList<>();
        categories.add("Alimentation");
        categories.add("Transport");
        categories.add("Loisirs");
        categories.add("Santé");
        attributes.add(new Attribute("categorie", categories));

        attributes.add(new Attribute("mois"));
        attributes.add(new Attribute("jour"));

        List<String> devises = new ArrayList<>();
        devises.add("EUR");
        devises.add("USD");
        attributes.add(new Attribute("devise", devises));

        attributes.add(new Attribute("montant")); // valeur cible

        datasetStructure = new Instances("Transactions", attributes, 0);
        datasetStructure.setClassIndex(4); // "montant" est la cible

        // 🔹 Ajouter les transactions existantes comme données d'entraînement
        for (Transaction t : transactions) {
            double[] vals = new double[datasetStructure.numAttributes()];
            vals[0] = datasetStructure.attribute(0).indexOfValue(t.getCategorie());
            vals[1] = t.getDateTransaction().getMonthValue();
            vals[2] = t.getDateTransaction().getDayOfMonth();
            vals[3] = datasetStructure.attribute(3).indexOfValue(t.getDevise());
            vals[4] = t.getMontant();

            datasetStructure.add(new DenseInstance(1.0, vals));
        }

        // 🔹 Convertir les attributs nominales en binaires
        NominalToBinary filter = new NominalToBinary();
        filter.setInputFormat(datasetStructure);
        datasetStructure = Filter.useFilter(datasetStructure, filter);

        // 🔹 Entraîner le modèle
        modele = new LinearRegression();
        modele.buildClassifier(datasetStructure);
    }

    /**
     * Prédit un montant à partir de catégorie, date et devise
     */
    public double predireMontant(String categorie, int mois, int jour, String devise) throws Exception {
        double[] vals = new double[datasetStructure.numAttributes()];

        // Attention : après NominalToBinary, les index ont changé !
        vals[0] = datasetStructure.attribute(0).indexOfValue(categorie);
        vals[1] = mois;
        vals[2] = jour;
        vals[3] = datasetStructure.attribute(3).indexOfValue(devise);
        vals[4] = Double.NaN;

        DenseInstance instance = new DenseInstance(1.0, vals);
        instance.setDataset(datasetStructure);

        return modele.classifyInstance(instance);
    }
}
