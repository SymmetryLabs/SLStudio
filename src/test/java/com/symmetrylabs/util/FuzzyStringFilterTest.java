package com.symmetrylabs.util;

import org.junit.Test;
import org.junit.Assert;

public class FuzzyStringFilterTest {
    @Test
    public void testSimpleSentence() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        fsf.addSentence(0, "cat", "dog");
        fsf.addSentence(1, "dog", "cat");
        fsf.addSentence(2, "dumb", "cat", "otter");
        fsf.addSentence(3, "dumb", "otter");
        fsf.setFilterText("cado");
        Assert.assertTrue(fsf.matches(0));
        Assert.assertTrue(fsf.matches(1));
        Assert.assertTrue(fsf.matches(2));
        Assert.assertFalse(fsf.matches(3));
    }

    @Test
    public void testMatchEachWordOnce() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        fsf.addSentence(0, "avocado", "thing");
        fsf.addSentence(1, "doca", "is", "a", "word");
        fsf.setFilterText("cado");
        Assert.assertTrue(fsf.matches(0));
        Assert.assertFalse(fsf.matches(1));
    }

    @Test
    public void testMatchEachWordOnceWithBacktracking() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        /* the naive approach matches "ca" in "doca" and then can't find a match
           for "do"; we want to make sure we find "do" in "doca" and "ca" in
           "evocative". */
        fsf.addSentence(0, "doca", "is", "an", "evocative", "word");
        fsf.setFilterText("cado");
        Assert.assertTrue(fsf.matches(0));
    }

    @Test
    public void testMiddleMatchLimit() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        fsf.addSentence(0, "every", "good", "boy", "does", "fine"); // ok: matches are all at the start
        fsf.addSentence(1, "xeg", "xbdf"); // ok: matches are >= 2
        fsf.addSentence(2, "egbd", "xfx"); // not ok: match for f is in the middle
        fsf.addSentence(3, "e", "xgbdfx"); // short words should work with normally-too-short matches
        fsf.setFilterText("egbdf");
        Assert.assertTrue(fsf.matches(0));
        Assert.assertTrue(fsf.matches(1));
        Assert.assertFalse(fsf.matches(2));
        Assert.assertTrue(fsf.matches(3));
    }

    @Test
    public void testCaseInsensitive() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        fsf.addSentence(0, "ABadCase");
        fsf.setFilterText("badc");
        Assert.assertTrue(fsf.matches(0));
    }

    @Test
    public void testEmptyFilter() {
        FuzzyStringFilter<Integer> fsf = new FuzzyStringFilter<Integer>();
        fsf.addSentence(0, "one", "two");
        fsf.addSentence(1, "threethreethree");
        fsf.setFilterText("");
        Assert.assertTrue(fsf.matches(0));
        Assert.assertTrue(fsf.matches(1));
    }
}
