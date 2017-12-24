package uk.co.mpcontracting.rpmjukebox.test.support;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class TestTermsEnum extends TermsEnum {

    private Iterator<BytesRef> iterator;

    public TestTermsEnum(Iterator<BytesRef> iterator) {
        this.iterator = iterator;
    }

    @Override
    public BytesRef next() throws IOException {
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    @Override
    public int docFreq() throws IOException {
        return 0;
    }

    @Override
    public long ord() throws IOException {
        return 0;
    }

    @Override
    public PostingsEnum postings(PostingsEnum reuse, int flags) throws IOException {
        return null;
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) throws IOException {
        return null;
    }

    @Override
    public void seekExact(long ord) throws IOException {
    }

    @Override
    public BytesRef term() throws IOException {
        return null;
    }

    @Override
    public long totalTermFreq() throws IOException {
        return 0;
    }
}
