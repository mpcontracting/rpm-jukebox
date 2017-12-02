package uk.co.mpcontracting.rpmjukebox.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class HashGeneratorTest extends AbstractTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionWithNullHashKeys() throws Exception {
		HashGenerator.generateHash((Object[])null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionWithNoHashKeys() throws Exception {
		HashGenerator.generateHash(new Object[] {});
	}
	
	@Test(expected = Exception.class)
	public void shouldThrowExceptionIfKeyLengthIsZero() throws Exception {
		HashGenerator.generateHash("", "");
	}
	
	@Test
	public void shouldGenerateAHashFromASingleObject() throws Exception {
		String hash = HashGenerator.generateHash("Object 1");
		
		assertThat("Generated hash should be '9c98295d0c3d33bf3ba088bfa61e7c781c6e6cc95d4cdc9ce98c1ee070424c4a'", hash, 
				equalTo("9c98295d0c3d33bf3ba088bfa61e7c781c6e6cc95d4cdc9ce98c1ee070424c4a"));
	}
	
	@Test
	public void shouldGenerateAHashFromAMultipleObjects() throws Exception {
		String hash = HashGenerator.generateHash("Object 1", "Object 2");

		assertThat("Generated hash should be '7ca30a03c43d539842c53db6597a7ea583fa4f2b37a2a63bf67d087538282e27'", hash, 
				equalTo("7ca30a03c43d539842c53db6597a7ea583fa4f2b37a2a63bf67d087538282e27"));
	}
	
	@Test
	public void shouldGenerateEqualHashesFromAMultipleObjectsWithNull() throws Exception {
		String hash1 = HashGenerator.generateHash("Object 1", "Object 2");
		String hash2 = HashGenerator.generateHash("Object 1", null, "Object 2");

		assertThat("Should generate the same hash if an key is null", hash1, equalTo(hash2));
	}
}
