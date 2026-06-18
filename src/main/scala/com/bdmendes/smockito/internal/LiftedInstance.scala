package com.bdmendes.smockito.internal

import scala.reflect.ClassTag

private[smockito] object LiftedInstance:

  // scalafmt: { maxColumn = 240 }

  def apply[T](obj: T)(using ct: ClassTag[T]): T =
    val proxy =
      obj match
        // Lambdas require special treatment as they are usually synthetic classes
        // that Mockito cannot copy.
        case f: Function0[?] =>
          ForwardingFunction0(f)
        case f: Function1[?, ?] =>
          ForwardingFunction1(f)
        case f: Function2[?, ?, ?] =>
          ForwardingFunction2(f)
        case f: Function3[?, ?, ?, ?] =>
          ForwardingFunction3(f)
        case f: Function4[?, ?, ?, ?, ?] =>
          ForwardingFunction4(f)
        case f: Function5[?, ?, ?, ?, ?, ?] =>
          ForwardingFunction5(f)
        case f: Function6[?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction6(f)
        case f: Function7[?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction7(f)
        case f: Function8[?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction8(f)
        case f: Function9[?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction9(f)
        case f: Function10[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction10(f)
        case f: Function11[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction11(f)
        case f: Function12[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction12(f)
        case f: Function13[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction13(f)
        case f: Function14[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction14(f)
        case f: Function15[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction15(f)
        case f: Function16[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction16(f)
        case f: Function17[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction17(f)
        case f: Function18[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction18(f)
        case f: Function19[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction19(f)
        case f: Function20[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction20(f)
        case f: Function21[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction21(f)
        case f: Function22[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
          ForwardingFunction22(f)
        case _ =>
          obj
    if ct.runtimeClass.isInstance(proxy) then
      proxy.asInstanceOf[T]
    else
      obj

  class ForwardingFunction0[R](f: () => R) extends Function0[R]:
    override def apply(): R = f()

  class ForwardingFunction1[T1, R](f: T1 => R) extends Function1[T1, R]:
    override def apply(v1: T1): R = f(v1)

  class ForwardingFunction2[T1, T2, R](f: (T1, T2) => R) extends Function2[T1, T2, R]:
    override def apply(v1: T1, v2: T2): R = f(v1, v2)

  class ForwardingFunction3[T1, T2, T3, R](f: (T1, T2, T3) => R) extends Function3[T1, T2, T3, R]:
    override def apply(v1: T1, v2: T2, v3: T3): R = f(v1, v2, v3)

  class ForwardingFunction4[T1, T2, T3, T4, R](f: (T1, T2, T3, T4) => R) extends Function4[T1, T2, T3, T4, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4): R = f(v1, v2, v3, v4)

  class ForwardingFunction5[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => R) extends Function5[T1, T2, T3, T4, T5, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5): R = f(v1, v2, v3, v4, v5)

  class ForwardingFunction6[T1, T2, T3, T4, T5, T6, R](f: (T1, T2, T3, T4, T5, T6) => R) extends Function6[T1, T2, T3, T4, T5, T6, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6): R = f(v1, v2, v3, v4, v5, v6)

  class ForwardingFunction7[T1, T2, T3, T4, T5, T6, T7, R](f: (T1, T2, T3, T4, T5, T6, T7) => R) extends Function7[T1, T2, T3, T4, T5, T6, T7, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7): R = f(v1, v2, v3, v4, v5, v6, v7)

  class ForwardingFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R](f: (T1, T2, T3, T4, T5, T6, T7, T8) => R) extends Function8[T1, T2, T3, T4, T5, T6, T7, T8, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8): R = f(v1, v2, v3, v4, v5, v6, v7, v8)

  class ForwardingFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R) extends Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9)

  class ForwardingFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R) extends Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)

  class ForwardingFunction11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R) extends Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)

  class ForwardingFunction12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R) extends Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]:
    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)

  class ForwardingFunction13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R) extends Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)

  class ForwardingFunction14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R)
      extends Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)

  class ForwardingFunction15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R)
      extends Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15): R = f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)

  class ForwardingFunction16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R)
      extends Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)

  class ForwardingFunction17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R)
      extends Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)

  class ForwardingFunction18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R)
      extends Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17, v18: T18): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)

  class ForwardingFunction19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R)
      extends Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17, v18: T18, v19: T19): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)

  class ForwardingFunction20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R)
      extends Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17, v18: T18, v19: T19, v20: T20): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)

  class ForwardingFunction21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R)
      extends Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17, v18: T18, v19: T19, v20: T20, v21: T21): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)

  class ForwardingFunction22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](
      f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R
  ) extends Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]:

    override def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8, v9: T9, v10: T10, v11: T11, v12: T12, v13: T13, v14: T14, v15: T15, v16: T16, v17: T17, v18: T18, v19: T19, v20: T20, v21: T21, v22: T22): R =
      f(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)
