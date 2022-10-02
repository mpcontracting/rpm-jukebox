package uk.co.mpcontracting.rpmjukebox.test.support;

import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.AttributeSource;
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
    public ImpactsEnum impacts(int flags) {
        return null;
    }

    @Override
    public TermState termState() {
        return null;
    }

    @Override
    public AttributeSource attributes() {
        return null;
    }

    @Override
    public boolean seekExact(BytesRef text) {
        return false;
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) {
        return null;
    }

    @Override
    public void seekExact(long ord) {
    }

    @Override
    public void seekExact(BytesRef term, TermState state) {

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
