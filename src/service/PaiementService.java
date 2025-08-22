package service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class PaiementService {

    public PaiementService(String apiKey) {
        Stripe.apiKey = "STRIPE_API_KEY";
; // clé API Stripe
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
