package com.bdmendes.smockito.internal

class MockitoAgentSpec extends munit.FunSuite:

  test("premain method exists"):
    assert(MockitoAgent.mockitoPremain != null)
