package gcd

import chisel3._
import chiseltest._
import chiseltest.formal._
import org.scalatest.flatspec.AnyFlatSpec

class GCDFormalSpec extends AnyFlatSpec with Formal with ChiselScalatestTester {
    behavior of "GCD"
    it should "pass" in {
        verify(new GCD(), Seq(BoundedCheck(5), Z3EngineAnnotation))
    }
}

