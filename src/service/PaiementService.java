package service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PaiementService {
    private String stripeApiKey;

    public PaiementService() {
        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(".env");
            props.load(fis);
            stripeApiKey = props.getProperty("STRIPE_API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
            stripeApiKey = "CLE_PAR_DEFAUT";
        }

        Stripe.apiKey = stripeApiKey;
    }

    public PaymentIntent creerPaiement(long montantCentimes, String devise, String description) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(montantCentimes)
                        .setCurrency(devise)
                        .setDescription(description)
                        .build();
        return PaymentIntent.create(params);
    }
}
