package uk.co.mpcontracting.rpmjukebox.test.util;

import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOBooleanSupplier;

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
  public ImpactsEnum impacts(int i) throws IOException {
    return null;
  }

  @Override
  public TermState termState() throws IOException {
    return null;
  }

  @Override
  public AttributeSource attributes() {
    return null;
  }

  @Override
  public boolean seekExact(BytesRef bytesRef) throws IOException {
    return false;
  }

  @Override
  public IOBooleanSupplier prepareSeekExact(BytesRef bytesRef) throws IOException {
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
  public void seekExact(BytesRef bytesRef, TermState termState) throws IOException {

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