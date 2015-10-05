package org.eclipse.jdt.core.examples;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyIntegerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testMyInteger() {
		final MyInteger i = new MyInteger(3);
		assertTrue(i.getValue() == 3);
	}

	@Test
	public void testAddMyInteger() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(3);
		final MyInteger result = i + j;
		assertTrue(result.getValue() == 4);
	}
	
	@Test
	public void testAddInt() {
		final MyInteger i = new MyInteger(1);
		final int j = 3;
		final MyInteger result = i + j;
		assertTrue(result.getValue() == 4);
	}
	
	

}
