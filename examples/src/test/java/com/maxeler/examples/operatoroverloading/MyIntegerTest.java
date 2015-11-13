package com.maxeler.examples.operatoroverloading;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.maxeler.examples.operatoroverloading.MyInteger;

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
		new MyInteger(3);
	}
	
	@Test
	public void testGetValue() {
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
	
	@Test
	public void testAddIntAsRightHandSide() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final MyInteger result = j + i;
		assertTrue(result.getValue() == 4);
	}
	
	@Test
	public void testAddFloat() {
		final MyInteger i = new MyInteger(1);
		final float f = 3.7f;
		final MyInteger result = i + f;
		assertTrue(result.getValue() == 4);
	}
	
	@Test
	public void testAddFloatAsRightHandSide() {
		final float f = 3.7f;
		final MyInteger i = new MyInteger(1);
		final MyInteger result = f + i;
		assertTrue(result.getValue() == 4);
	}
	
	@Test
	public void testAndMyInteger() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(3);
		final MyInteger result = i & j;
		assertTrue(result.getValue() == 1);
	}
	
	@Test
	public void testAndInt() {
		final MyInteger i = new MyInteger(1);
		final int j = 3;
		final MyInteger result = i & j;
		assertTrue(result.getValue() == 1);
	}
	
	@Test
	public void testAndIntAsRightAndSide() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final MyInteger result = j & i;
		assertTrue(result.getValue() == 1);
	}
	
	@Test
	public void testDivMyInteger() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(3);
		final MyInteger result = i / j;
		assertTrue(result.getValue() == 0);
	}
	
	@Test
	public void testDivInt() {
		final MyInteger i = new MyInteger(1);
		final int j = 3;
		final MyInteger result = i / j;
		assertTrue(result.getValue() == 0);		
	}
	
	@Test
	public void testDivIntAsRightAndSide() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final MyInteger result = j / i;
		assertTrue(result.getValue() == 3);
	}
	
	@Test
	public void testEqMyInteger() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(3);
		final boolean result = i === j;
		assertFalse(result);
	}
	
	@Test
	public void testEqInt() {
		final MyInteger i = new MyInteger(1);
		final int j = 3;
		final boolean result = i === j;
		assertFalse(result);
	}
	
	@Test
	public void testEqIntAsRightHandSide() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final boolean result = j === i;
		assertFalse(result);
	}
	
	@Test
	public void testNeqMyInteger() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(3);
		final boolean result = i !== j;
		assertTrue(result);
	}
	
	@Test
	public void testNeqInt() {
		final MyInteger i = new MyInteger(1);
		final int j = 3;
		final boolean result = i !== j;
		assertTrue(result);
	}
	
	@Test
	public void testNeqIntAsRightHandSide() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final boolean result = j !== i;
		assertTrue(result);
	}
	
	@Test
	public void testGtMyIntegerWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final MyInteger j = new MyInteger(1);
		final boolean result = i > j;
		assertFalse(result);
	}
	
	@Test
	public void testGtMyIntegerWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(1);
		final boolean result = i > j;
		assertFalse(result);
	}
	
	@Test
	public void testGtMyIntegerWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final MyInteger j = new MyInteger(1);
		final boolean result = i > j;
		assertTrue(result);
	}
	
	@Test
	public void testGtIntWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final int j = 1;
		final boolean result = i > j;
		assertFalse(result);
	}
	
	@Test
	public void testGtIntWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final int j = 1;
		final boolean result = i > j;
		assertFalse(result);
	}
	
	@Test
	public void testGtIntWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final int j = 1;
		final boolean result = i > j;
		assertTrue(result);
	}
	
	@Test
	public void testGtIntAsRightHandSideWhenILessThanJ() {
		final int j = 3;
		final MyInteger i = new MyInteger(1);
		final boolean result = j > i;
		assertTrue(result);
	}
	
	@Test
	public void testGtIntAsRightHandSideWhenIEqualsJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(1);
		final boolean result = j > i;
		assertFalse(result);
	}
	
	@Test
	public void testGtIntAsRightHandSideWhenIGreaterThanJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(3);
		final boolean result = j > i;
		assertFalse(result);
	}
	
	@Test
	public void testGteMyIntegerWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final MyInteger j = new MyInteger(1);
		final boolean result = i >= j;
		assertFalse(result);
	}
	
	@Test
	public void testGteMyIntegerWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(1);
		final boolean result = i >= j;
		assertTrue(result);
	}
	
	@Test
	public void testGteMyIntegerWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final MyInteger j = new MyInteger(1);
		final boolean result = i >= j;
		assertTrue(result);
	}
	
	@Test
	public void testGteIntWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final int j = 1;
		final boolean result = i >= j;
		assertFalse(result);
	}
	
	@Test
	public void testGteIntWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final int j = 1;
		final boolean result = i >= j;
		assertTrue(result);
	}
	
	@Test
	public void testGteIntWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final int j = 1;
		final boolean result = i >= j;
		assertTrue(result);
	}
	
	@Test
	public void testGteIntAsRHSWhenILessThanJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(0);
		final boolean result = j >= i;
		assertTrue(result);
	}
	
	@Test
	public void testGteIntAsRHSWhenIEqualsJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(1);
		final boolean result = j >= i;
		assertTrue(result);
	}
	
	@Test
	public void testGteIntAsRHSWhenIGreaterThanJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(2);
		final boolean result = j >= i;
		assertFalse(result);
	}
	
	@Test
	public void testLtMyIntegerWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final MyInteger j = new MyInteger(1);
		final boolean result = i < j;
		assertTrue(result);
	}
	
	@Test
	public void testLtMyIntegerWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(1);
		final boolean result = i < j;
		assertFalse(result);
	}
	
	@Test
	public void testLtMyIntegerWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final MyInteger j = new MyInteger(1);
		final boolean result = i < j;
		assertFalse(result);
	}
	
	@Test
	public void testLtIntWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final int j = 1;
		final boolean result = i < j;
		assertTrue(result);
	}
	
	@Test
	public void testLtIntWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final int j = 1;
		final boolean result = i < j;
		assertFalse(result);
	}
	
	@Test
	public void testLtIntWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final int j = 1;
		final boolean result = i < j;
		assertFalse(result);
	}
	
	@Test
	public void testLtIntAsRHSWhenILessThanJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(0);
		final boolean result = j < i;
		assertFalse(result);
	}
	
	@Test
	public void testLtIntAsRHSWhenIEqualsJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(1);
		final boolean result = j < i;
		assertFalse(result);
	}
	
	@Test
	public void testLtIntAsRHSWhenIGreaterThanJ() {
		final int j = 1;
		final MyInteger i = new MyInteger(2);
		final boolean result = j < i;
		assertTrue(result);
	}
	
	@Test
	public void testLteMyIntegerWhenILessThanJ() {
		final MyInteger i = new MyInteger(0);
		final MyInteger j = new MyInteger(1);
		final boolean result = i <= j;
		assertTrue(result);
	}
	
	@Test
	public void testLteMyIntegerWhenIEqualsJ() {
		final MyInteger i = new MyInteger(1);
		final MyInteger j = new MyInteger(1);
		final boolean result = i <= j;
		assertTrue(result);
	}
	
	@Test
	public void testLteMyIntegerWhenIGreaterThanJ() {
		final MyInteger i = new MyInteger(2);
		final MyInteger j = new MyInteger(1);
		final boolean result = i <= j;
		assertFalse(result);
	}
}
