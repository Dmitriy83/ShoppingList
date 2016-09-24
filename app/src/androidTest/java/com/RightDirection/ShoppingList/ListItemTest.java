package com.RightDirection.ShoppingList;

import android.net.Uri;
import android.os.Parcelable;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListItemTest {

    private ListItem listItem;

    @Before
    public void setUp() throws Exception {
        listItem = new ListItem(999999, "testName", Uri.parse("testStringUri"));
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(999999, listItem.getId());
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

    @Test
    public void testSetGetCount() throws Exception {
        assertEquals(1, listItem.getCount(), 0);
        listItem.setCount(7);
        assertEquals(7, listItem.getCount(), 0);
        listItem.setCount(0);
        assertEquals(0, listItem.getCount(), 0);
        listItem.setCount(3);
        assertEquals(3, listItem.getCount(), 0);
        listItem.setCount(-5);
        assertEquals(3, listItem.getCount(), 0);
        listItem.setCount("5");
        assertEquals(5, listItem.getCount(), 0);
        listItem.setCount("s5");
        assertEquals(5, listItem.getCount(), 0);
    }
}