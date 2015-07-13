package com.billybyte.commonlibstometeor;

import junit.framework.TestCase;

public class TestMextends extends TestCase {
	static abstract class ClassMain {
		abstract <M extends ClassMain> M myMethod(int i);
		int myInt;
	}
	static class ClassSub extends ClassMain{

		@Override
		ClassSub myMethod(int i) {
			ClassSub cs = new ClassSub();
			System.out.print("i = " + i);
			return cs;
		}
		
	}
	public void test1(){
		ClassSub cs = new ClassSub();
		cs.myMethod(10);
	}
}
