package io.prometheus.jmx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RulePatternCache {
    /**
     * We store computed patterns and their results in the following map
     */
    private final Map<PatternMatch, RegexResult> patternToMatches;

    public RulePatternCache() {
        patternToMatches = new ConcurrentHashMap<PatternMatch, RegexResult>();
    }

    // Return if pattern matches matchName. If nextCache is not null, also
    // place the cache entry in nextCache as well as this cache.
    public boolean checkAndStoreMatchName(Pattern pattern, String matchName, RulePatternCache nextCache) {
        PatternMatch patternMatch = new PatternMatch(pattern, matchName);
        RegexResult regexResult = patternToMatches.get(patternMatch);
        if (regexResult == null) {
            Matcher matcher = pattern.matcher(matchName);
            boolean result = matcher.matches();
            regexResult = new RegexResult(matcher, result);
            patternToMatches.put(patternMatch, regexResult);
        }
        if (nextCache != null) {
            nextCache.patternToMatches.put(patternMatch, regexResult);
        }
        return regexResult.getResult();
    }

    public Matcher getMatcher(Pattern pattern, String match) {
        return patternToMatches.get(new PatternMatch(pattern, match)).getMatcher();
    }

    /**
     * This is the key of the cache
     */
    private class PatternMatch {
        private final Pattern pattern;
        private final String match;

        private PatternMatch(Pattern pattern, String match) {
            this.pattern = pattern;
            this.match = match;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatternMatch that = (PatternMatch) o;
            return com.google.common.base.Objects.equal(pattern, that.pattern) &&
                    com.google.common.base.Objects.equal(match, that.match);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(pattern, match);
        }
    }

    /**
     * This is the value of the cache
     */
    private class RegexResult {
        private final Matcher matcher;
        private final Boolean result;

        private RegexResult(Matcher matcher, Boolean result) {
            this.matcher = matcher;
            this.result = result;
        }

        public Matcher getMatcher() {
            return matcher;
        }

        public Boolean getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegexResult that = (RegexResult) o;
            return com.google.common.base.Objects.equal(matcher, that.matcher) &&
                    com.google.common.base.Objects.equal(result, that.result);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(matcher, result);
        }
    }
}
