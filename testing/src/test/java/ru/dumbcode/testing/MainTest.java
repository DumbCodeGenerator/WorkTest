package ru.dumbcode.testing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class MainTest {

    @Test
    public void testSumMarksValues() {
        Map<String, ArrayList<Integer>> testList = new TreeMap<String, ArrayList<Integer>>(String.CASE_INSENSITIVE_ORDER);
        ArrayList<Integer> array1 = new ArrayList<>(Arrays.asList(new Integer[] {2, 10, 65, 44}));
        ArrayList<Integer> array2 = new ArrayList<>(Arrays.asList(new Integer[] {5, 8, 25, 33}));
        testList.put("mark10", array1);
        testList.put("markFX", array2);
        
        assertEquals("{mark10=121, markFX=71}", Main.sumMarksValues(testList).toString());
    }

    @Test
    public void testAddToList() {
        Map<String, ArrayList<Integer>> testList = new TreeMap<String, ArrayList<Integer>>(String.CASE_INSENSITIVE_ORDER);
        ArrayList<Integer> array1 = new ArrayList<>(Arrays.asList(2, 10));
        ArrayList<Integer> array2 = new ArrayList<>(Arrays.asList(5, 8));
        testList.put("mark10", array1);
        testList.put("markFX", array2);
        
        assertEquals("{mark10=[2, 10, 44], markFX=[5, 8]}", Main.addToList(testList, "mark10", 44).toString());
        assertEquals("{mark10=[2, 10, 44], markFX=[5, 8, 33]}", Main.addToList(testList, "markFX", 33).toString());
    }

}
