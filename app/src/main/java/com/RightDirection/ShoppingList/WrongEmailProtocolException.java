package com.RightDirection.ShoppingList;

public class WrongEmailProtocolException extends Exception{
    public WrongEmailProtocolException()
    {
        super("Supports only IMAP protocol.");
    }
}
