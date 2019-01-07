package com.shareThegame.STG.Model;

import lombok.Data;

import javax.persistence.Entity;

@Data
public class ChargeRequest {

    public enum Currency {
        PLN;
    }
    private String description;
    private int amount;

    public
    int getPaymentid ( ) {
        return paymentid;
    }

    public
    void setPaymentid ( int paymentid ) {
        this.paymentid = paymentid;
    }

    private  int paymentid;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;


    public
    String getDescription ( ) {
        return description;
    }

    public
    void setDescription ( String description ) {
        this.description = description;
    }

    public
    int getAmount ( ) {
        return amount;
    }

    public
    void setAmount ( int amount ) {
        this.amount = amount;
    }

    public
    Currency getCurrency ( ) {
        return currency;
    }

    public
    void setCurrency ( Currency currency ) {
        this.currency = currency;
    }

    public
    String getStripeEmail ( ) {
        return stripeEmail;
    }

    public
    void setStripeEmail ( String stripeEmail ) {
        this.stripeEmail = stripeEmail;
    }

    public
    String getStripeToken ( ) {
        return stripeToken;
    }

    public
    void setStripeToken ( String stripeToken ) {
        this.stripeToken = stripeToken;
    }
}
