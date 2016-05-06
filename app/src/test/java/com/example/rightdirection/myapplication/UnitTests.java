package com.example.rightdirection.myapplication;

import com.RightDirection.ShoppingList.ListItem;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UnitTests {
    @Test
    public void listItemCreation_isCorrect() throws Exception {
        String id = "120";
        String name = "TestName";
        ListItem listItem = new ListItem(id, name);

        assertEquals(id, listItem.getId());
        assertEquals(name, listItem.getName());
    }
}