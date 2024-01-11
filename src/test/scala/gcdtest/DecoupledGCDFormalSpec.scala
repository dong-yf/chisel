package gcd

import chisel3._
import chiseltest._
import chiseltest.formal._
import org.scalatest.flatspec.AnyFlatSpec

class DecoupledGCDFormalSpec extends AnyFlatSpec with Formal with ChiselScalatestTester {
    behavior of "DecoupledGCD"
    it should "pass" in {
        verify(new DecoupledGcd(16), Seq(BoundedCheck(5), Z3EngineAnnotation))
    }
}