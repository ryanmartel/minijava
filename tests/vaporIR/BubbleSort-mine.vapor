func Main()
  t.1 = HeapAllocZ(12)
  [t.1] = :functable_BBS
  if t.1 goto :null1
  Error("null pointer")
  null1:
  t.2 = [t.1]
  t.2 = [t.2+0]
  t.3 = call t.2(t.1 10)
  PrintIntS(t.3)
  ret

func BBS_Start(this sz)
  if this goto :null2
  Error("null pointer")
  null2:
  t.1 = [this]
  t.1 = [t.1+12]
  t.2 = call t.1(this sz)
  aux01 = t.2
  if this goto :null3
  Error("null pointer")
  null3:
  t.3 = [this]
  t.3 = [t.3+8]
  t.4 = call t.3(this)
  aux01 = t.4
  PrintIntS(99999)
  if this goto :null4
  Error("null pointer")
  null4:
  t.5 = [this]
  t.5 = [t.5+4]
  t.6 = call t.5(this)
  aux01 = t.6
  if this goto :null5
  Error("null pointer")
  null5:
  t.7 = [this]
  t.7 = [t.7+8]
  t.8 = call t.7(this)
  aux01 = t.8
  ret 0

func BBS_Sort(this)
  t.1 = [this+8]
  t.2 = Sub(t.1 1)
  i = t.2
  t.3 = Sub(0 1)
  aux02 = t.3
  while1_top:
  t.4 = LtS(aux02 i)
  if0 t.4 goto :while1_end
  j = 1
  while2_top:
  t.5 = Add(i 1)
  t.6 = LtS(j t.5)
  if0 t.6 goto :while2_end
  t.7 = Sub(j 1)
  aux07 = t.7
  t.8 = [this+4]
  if t.8 goto :null6
  Error("null pointer")
  null6:
  t.9 = [t.8]
  t.9 = LtS(aux07 t.9)
  if t.9 goto :bounds1
  Error("array index out of bounds")
  bounds1:
  t.9 = MulS(aux07 4)
  t.9 = Add(t.9 t.8)
  t.10 = [t.9+4]
  aux04 = t.10
  t.11 = [this+4]
  if t.11 goto :null7
  Error("null pointer")
  null7:
  t.12 = [t.11]
  t.12 = LtS(j t.12)
  if t.12 goto :bounds2
  Error("array index out of bounds")
  bounds2:
  t.12 = MulS(j 4)
  t.12 = Add(t.12 t.11)
  t.13 = [t.12+4]
  aux05 = t.13
  t.14 = LtS(aux05 aux04)
  if0 t.14 goto :if1_else
  t.15 = Sub(j 1)
  aux06 = t.15
  t.16 = [this+4]
  if t.16 goto :null8
  Error("null pointer")
  null8:
  t.17 = [t.16]
  t.17 = LtS(aux06 t.17)
  if t.17 goto :bounds3
  Error("array index out of bounds")
  bounds3:
  t.17 = MulS(aux06 4)
  t.17 = Add(t.17 t.16)
  t.18 = [t.17+4]
  t = t.18
  t.19 = [this+4]
  if t.19 goto :null9
  Error("null pointer")
  null9:
  t.20 = [t.19]
  t.20 = Lt(aux06 t.20)
  if t.20 goto :bounds4
  Error("array index out of bounds")
  bounds4:
  t.20 = MulS(aux06 4)
  t.20 = Add(t.20 t.19)
  t.21 = [this+4]
  if t.21 goto :null10
  Error("null pointer")
  null10:
  t.22 = [t.21]
  t.22 = LtS(j t.22)
  if t.22 goto :bounds5
  Error("array index out of bounds")
  bounds5:
  t.22 = MulS(j 4)
  t.22 = Add(t.22 t.21)
  t.23 = [t.22+4]
  [t.20+4] = t.23
  t.24 = [this+4]
  if t.24 goto :null11
  Error("null pointer")
  null11:
  t.25 = [t.24]
  t.25 = Lt(j t.25)
  if t.25 goto :bounds6
  Error("array index out of bounds")
  bounds6:
  t.25 = MulS(j 4)
  t.25 = Add(t.25 t.24)
  [t.25+4] = t
  goto :if1_end
  if1_else:
  nt = 0
  if1_end:
  t.26 = Add(j 1)
  j = t.26
  goto :while2_top
  while2_end:
  t.27 = Sub(i 1)
  i = t.27
  goto :while1_top
  while1_end:
  ret 0

func BBS_Print(this)
  j = 0
  while3_top:
  t.1 = [this+8]
  t.2 = LtS(j t.1)
  if0 t.2 goto :while3_end
  t.3 = [this+4]
  if t.3 goto :null12
  Error("null pointer")
  null12:
  t.4 = [t.3]
  t.4 = LtS(j t.4)
  if t.4 goto :bounds7
  Error("array index out of bounds")
  bounds7:
  t.4 = MulS(j 4)
  t.4 = Add(t.4 t.3)
  t.5 = [t.4+4]
  PrintIntS(t.5)
  t.6 = Add(j 1)
  j = t.6
  goto :while3_top
  while3_end:
  ret 0

func BBS_Init(this sz)
  [this+8] = sz
  t.1 = call :AllocArray(sz)
  [this+4] = t.1
  t.2 = [this+4]
  if t.2 goto :null13
  Error("null pointer")
  null13:
  t.3 = [t.2]
  t.3 = Lt(0 t.3)
  if t.3 goto :bounds8
  Error("array index out of bounds")
  bounds8:
  t.3 = MulS(0 4)
  t.3 = Add(t.3 t.2)
  [t.3+4] = 20
  t.4 = [this+4]
  if t.4 goto :null14
  Error("null pointer")
  null14:
  t.5 = [t.4]
  t.5 = Lt(1 t.5)
  if t.5 goto :bounds9
  Error("array index out of bounds")
  bounds9:
  t.5 = MulS(1 4)
  t.5 = Add(t.5 t.4)
  [t.5+4] = 7
  t.6 = [this+4]
  if t.6 goto :null15
  Error("null pointer")
  null15:
  t.7 = [t.6]
  t.7 = Lt(2 t.7)
  if t.7 goto :bounds10
  Error("array index out of bounds")
  bounds10:
  t.7 = MulS(2 4)
  t.7 = Add(t.7 t.6)
  [t.7+4] = 12
  t.8 = [this+4]
  if t.8 goto :null16
  Error("null pointer")
  null16:
  t.9 = [t.8]
  t.9 = Lt(3 t.9)
  if t.9 goto :bounds11
  Error("array index out of bounds")
  bounds11:
  t.9 = MulS(3 4)
  t.9 = Add(t.9 t.8)
  [t.9+4] = 18
  t.10 = [this+4]
  if t.10 goto :null17
  Error("null pointer")
  null17:
  t.11 = [t.10]
  t.11 = Lt(4 t.11)
  if t.11 goto :bounds12
  Error("array index out of bounds")
  bounds12:
  t.11 = MulS(4 4)
  t.11 = Add(t.11 t.10)
  [t.11+4] = 2
  t.12 = [this+4]
  if t.12 goto :null18
  Error("null pointer")
  null18:
  t.13 = [t.12]
  t.13 = Lt(5 t.13)
  if t.13 goto :bounds13
  Error("array index out of bounds")
  bounds13:
  t.13 = MulS(5 4)
  t.13 = Add(t.13 t.12)
  [t.13+4] = 11
  t.14 = [this+4]
  if t.14 goto :null19
  Error("null pointer")
  null19:
  t.15 = [t.14]
  t.15 = Lt(6 t.15)
  if t.15 goto :bounds14
  Error("array index out of bounds")
  bounds14:
  t.15 = MulS(6 4)
  t.15 = Add(t.15 t.14)
  [t.15+4] = 6
  t.16 = [this+4]
  if t.16 goto :null20
  Error("null pointer")
  null20:
  t.17 = [t.16]
  t.17 = Lt(7 t.17)
  if t.17 goto :bounds15
  Error("array index out of bounds")
  bounds15:
  t.17 = MulS(7 4)
  t.17 = Add(t.17 t.16)
  [t.17+4] = 9
  t.18 = [this+4]
  if t.18 goto :null21
  Error("null pointer")
  null21:
  t.19 = [t.18]
  t.19 = Lt(8 t.19)
  if t.19 goto :bounds16
  Error("array index out of bounds")
  bounds16:
  t.19 = MulS(8 4)
  t.19 = Add(t.19 t.18)
  [t.19+4] = 19
  t.20 = [this+4]
  if t.20 goto :null22
  Error("null pointer")
  null22:
  t.21 = [t.20]
  t.21 = Lt(9 t.21)
  if t.21 goto :bounds17
  Error("array index out of bounds")
  bounds17:
  t.21 = MulS(9 4)
  t.21 = Add(t.21 t.20)
  [t.21+4] = 5
  ret 0

const functable_BBS
  :BBS_Start
  :BBS_Sort
  :BBS_Print
  :BBS_Init

func AllocArray(size)
  bytes = MulS(size 4)
  bytes = Add(bytes 4)
  v = HeapAllocZ(bytes)
  [v] = size
  ret v

