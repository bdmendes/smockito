package com.bdmendes.smockito.internal

import munit.FunSuite

class MockitoAgentSpec extends FunSuite:

  test("find the Mockito agent method"):
    assert(MockitoAgent.method.isSuccess)
