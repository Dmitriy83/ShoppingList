package com.RightDirection.ShoppingList.utils;

public class WrongEmailProtocolException extends Exception{
    public WrongEmailProtocolException()
    {
        super("Supports only IMAP protocol.");
    }
}
