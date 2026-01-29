/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.threads;

import java.util.stream.IntStream;

import lombok.AllArgsConstructor;

/**
 *
 * @author hcadavid
 */
@AllArgsConstructor
public class CountThread extends Thread {
    private int A;
    private int B;
    @Override
    public void run(){
        IntStream.range(A,B)
            .forEach(i -> System.out.println(
                Thread.currentThread().getName() + " : " + i));
    }
}
