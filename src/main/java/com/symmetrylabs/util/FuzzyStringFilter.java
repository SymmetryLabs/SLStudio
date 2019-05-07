package com.symmetrylabs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.collections4.iterators.PermutationIterator;
import java.util.Iterator;


/**
 * A fuzzy autocomplete algorithm for interactive filtering.
 *
 * FuzzyStringFilter takes a list of things that can be matched against and a
 * filter string, and determines which things in its set can be matched against.
 * The list of candidates are called "sentences" because they are collections of
 * substrings: the matching algorithm exploits the sentence structure of the
 * available choices to provide better matches.
 *
 * Given a set of sentences and a filter pattern, the algorithm implemented here
 * marks a sentence as matching if the pattern is a concatenation of substrings
 * of a permutation of words in the sentence. It also places a lower bound on
 * the size of the match between the pattern and each word when the match is not
 * at the start of the word. This means that the sentence "solid color" is
 * matched by "color", "socol", "sc", and "colsol", but not "dilcol", "soll", or
 * "oo".
 */
public class FuzzyStringFilter<T> {
    private static final int MIN_MIDDLE_MATCH_LEN = 2;

    private class Sentence {
        T id;
        String[] words;
        boolean[] wordsMatched;
        boolean matches;

        Sentence(T id, String[] words) {
            this.id = id;
            this.words = words;
            wordsMatched = new boolean[words.length];
            matches = false;
        }
    };

    private String filterText;
    private Map<T, Sentence> sentences;
    private boolean matchesReady;
    private Map<Pair<String, Integer>, List<List<String>>> splitCache = new HashMap<>();

    public FuzzyStringFilter() {
        sentences = new HashMap<>();
        matchesReady = false;
    }

    public void setFilterText(String newFilterText) {
        filterText = newFilterText.toLowerCase();
        if (matchesReady) {
            run();
        }
    }

    public void addSentence(T id, Collection<String> words) {
        String[] wordArray = new String[words.size()];
        words.toArray(wordArray);
        addSentence(id, wordArray);
    }

    public void addSentence(T id, String... words) {
        Preconditions.checkArgument(!sentences.containsKey(id));
        Sentence s = new Sentence(id, words);
        for (int i = 0; i < s.words.length; i++) {
            s.words[i] = s.words[i].toLowerCase();
        }
        sentences.put(id, s);
        if (matchesReady) {
            run();
        }
    }

    public void run() {
        Preconditions.checkState(filterText != null);
        splitCache.clear();

        for (Sentence s : sentences.values()) {
            s.matches = false;
            int count = s.words.length;

            for (List<String> sp : split(filterText, count - 1)) {
                /* remove all zero-length chunks to cut down on permutation count (even though they don't do anything) */
                while (sp.remove("")) {}

                Iterator<List<String>> splitPermute = new PermutationIterator<String>(sp);
                while (splitPermute.hasNext()) {
                    s.matches = matchSplit(splitPermute.next(), s);
                    if (s.matches) {
                        System.out.println(String.format("%s <- %s", String.join(" ", s.words), String.join(":", sp)));
                        break;
                    }
                }

                if (s.matches) break;
            }
        }
        matchesReady = true;
    }

    private boolean matchSplit(List<String> split, Sentence s) {
        for (int i = 0; i < s.wordsMatched.length; i++) {
            s.wordsMatched[i] = false;
        }

        for (String chunk : split) {
            if (chunk.length() == 0) {
                continue;
            }

            boolean matched = false;
            for (int i = 0; i < s.words.length && !matched; i++) {
                /* Each word can only be used once */
                if (s.wordsMatched[i]) continue;

                /* Check for a prefix match (which can be any length) */
                matched = s.wordsMatched[i] = s.words[i].startsWith(chunk);
                if (matched) break;

                /* If this chunk is shorter than the middle match length and the word is at least as long as the
                   minimum length, we can't use this chunk on this word. Note that we allow small chunks to
                   match small words no matter how large MIN_MIDDLE_MATCH_LEN is. */
                if (s.words[i].length() >= MIN_MIDDLE_MATCH_LEN && chunk.length() < MIN_MIDDLE_MATCH_LEN) continue;

                /* Search for a substring match for large-enough chunks */
                matched = s.wordsMatched[i] = s.words[i].contains(chunk);
            }

            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private List<List<String>> split(String s, int splitsleft) {
        Pair<String, Integer> splitkey = Pair.of(s, splitsleft);
        if (splitCache.containsKey(splitkey)) {
            return splitCache.get(splitkey);
        }

        List<List<String>> res = new ArrayList<>();
        if (s.length() == 0 || splitsleft == 0) {
            List<String> end = new ArrayList<>();
            end.add(s);
            res.add(end);
        } else {
            for (int i = 0; i <= s.length(); i++) {
                String prefix = s.substring(0, i);
                String suffix = s.substring(i);
                for (List<String> suffixSplit : split(suffix, splitsleft - 1)) {
                    List<String> sp = new ArrayList<>();
                    sp.add(prefix);
                    sp.addAll(suffixSplit);
                    res.add(sp);
                }
            }
        }
        splitCache.put(splitkey, res);
        return res;
    }

    public boolean matches(T id) {
        Preconditions.checkState(matchesReady);
        Preconditions.checkArgument(sentences.containsKey(id));
        return sentences.get(id).matches;
    }
}
