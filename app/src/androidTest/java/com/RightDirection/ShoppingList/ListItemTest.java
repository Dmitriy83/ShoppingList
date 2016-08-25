package com.RightDirection.ShoppingList;

import android.net.Uri;
import android.os.Parcelable;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ListItemTest {

    private ListItem listItem;

    @Before
    public void setUp() throws Exception {
        listItem = new ListItem("testId", "testName", Uri.parse("testStringUri"));
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("testId", listItem.getId());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("testName", listItem.getName());
    }

    @Test
    public void testSetName() throws Exception {
        listItem.setName("testNameChanged");
        assertEquals("testNameChanged", listItem.getName());
    }

    @Test
    public void testSetImageUri() throws Exception {
        listItem.setImageUri(Uri.parse("testStringUriChanged"));
        assertEquals(Uri.parse("testStringUriChanged"), listItem.getImageUri());
    }

    @Test
    public void testSetChecked() throws Exception {
        assertFalse(listItem.isChecked());
        listItem.setChecked();
        assertTrue(listItem.isChecked());
    }

    @Test
    public void testSetUnchecked() throws Exception {
        listItem.setUnchecked();
        assertFalse(listItem.isChecked());
    }

    @Test
    public void testIsChecked() throws Exception {
        assertFalse(listItem.isChecked());
    }

    @Test
    public void testGetImageUri() throws Exception {
        assertEquals(Uri.parse("testStringUri"), listItem.getImageUri());
    }

    @Test
    public void testDescribeContents() throws Exception {
        assertEquals(0, listItem.describeContents());
    }

    @Test
    public void testWriteToParcel() throws Exception {
        Parcelable listItemParcelable = listItem;
        ListItem listItemConverted = (ListItem)listItemParcelable;
        assertEquals(listItem, listItemConverted);
    }
}