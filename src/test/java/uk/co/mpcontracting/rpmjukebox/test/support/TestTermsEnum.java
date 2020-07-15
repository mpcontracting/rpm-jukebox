package uk.co.mpcontracting.rpmjukebox.test.support;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;

public class TestTermsEnum extends TermsEnum {

    private final Iterator<BytesRef> iterator;

    public TestTermsEnum(Iterator<BytesRef> iterator) {
        this.iterator = iterator;
    }

    @Override
    public BytesRef next() {
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    @Override
    public int docFreq() {
        return 0;
    }

    @Override
    public long ord() {
        return 0;
    }

    @Override
    public PostingsEnum postings(PostingsEnum reuse, int flags) {
        return null;
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) {
        return null;
    }

    @Override
    public void seekExact(long ord) {
    }

    @Override
    public BytesRef term() {
        return null;
    }

    @Override
    public long totalTermFreq() {
        return 0;
    }
}
