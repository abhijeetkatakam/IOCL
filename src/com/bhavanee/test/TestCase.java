package com.bhavanee.test;
import java.io.IOException;
import java.lang.ProcessBuilder;

public class TestCase
{
	public static void main(String[] args) throws IOException 
	{
		ProcessBuilder p = new ProcessBuilder();
		System.out.println("Started EXE");
		p.command("C:\\Users\\erapami\\Desktop\\JSONToXML\\restcall.exe");
		p.start();
		System.out.println("Started EXE"); 
	}
}

