package com.bdmendes.smockito.internal

class MockitoAgentSpec extends munit.FunSuite:

  test("find the Mockito agent method"):
    assert(MockitoAgent.method.isSuccess)
