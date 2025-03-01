func Main()
  t.1 = HeapAllocZ(12)
  [t.1] = :functable_LS
  if t.1 goto :null1
  Error("null pointer")
  null1:
  t.2 = [t.1]
  t.3 = [t.2+0]
  t.4 = call t.3(t.1 10)
  PrintIntS(t.4)
  ret

func LS_Start(this sz)
  t.5 = [this]
  t.6 = [t.5+12]
  t.7 = call t.6(this sz)
  aux01 = t.7
  t.8 = [this]
  t.9 = [t.8+4]
  t.10 = call t.9(this)
  aux02 = t.10
  PrintIntS(9999)
  t.11 = [this]
  t.12 = [t.11+8]
  t.13 = call t.12(this 8)
  PrintIntS(t.13)
  t.14 = [this]
  t.15 = [t.14+8]
  t.16 = call t.15(this 12)
  PrintIntS(t.16)
  t.17 = [this]
  t.18 = [t.17+8]
  t.19 = call t.18(this 17)
  PrintIntS(t.19)
  t.20 = [this]
  t.21 = [t.20+8]
  t.22 = call t.21(this 50)
  PrintIntS(t.22)
  ret 55

func LS_Print(this)
  j = 1
  while1_top:
  t.23 = [this+8]
  t.24 = LtS(j t.23)
  if0 t.24 goto :while1_end
  t.25 = [this+4]
  if t.25 goto :null2
  Error("null pointer")
  null2:
  t.26 = [t.25]
  t.26 = LtS(j t.26)
  if t.26 goto :bounds1
  Error("array index out of bounds")
  bounds1:
  t.26 = MulS(j 4)
  t.26 = Add(t.26 t.25)
  t.27 = [t.26+4]
  PrintIntS(t.27)
  t.28 = Add(j 1)
  j = t.28
  goto :while1_top
  while1_end:
  ret 0

func LS_Search(this num)
  j = 1
  ls01 = 0
  ifound = 0
  while2_top:
  t.29 = [this+8]
  t.30 = LtS(j t.29)
  if0 t.30 goto :while2_end
  t.31 = [this+4]
  if t.31 goto :null3
  Error("null pointer")
  null3:
  t.32 = [t.31]
  t.32 = LtS(j t.32)
  if t.32 goto :bounds2
  Error("array index out of bounds")
  bounds2:
  t.32 = MulS(j 4)
  t.32 = Add(t.32 t.31)
  t.33 = [t.32+4]
  aux01 = t.33
  t.34 = Add(num 1)
  aux02 = t.34
  t.35 = LtS(aux01 num)
  if0 t.35 goto :if1_else
  nt = 0
  goto :if1_end
  if1_else:
  t.36 = LtS(aux01 aux02)
  t.37 = Sub(1 t.36)
  if0 t.37 goto :if2_else
  nt = 0
  goto :if2_end
  if2_else:
  ls01 = 1
  ifound = 1
  t.38 = [this+8]
  j = t.38
  if2_end:
  if1_end:
  t.39 = Add(j 1)
  j = t.39
  goto :while2_top
  while2_end:
  ret ifound

func LS_Init(this sz)
  [this+8] = sz
  t.40 = call :AllocArray(sz)
  [this+4] = t.40
  j = 1
  t.41 = [this+8]
  t.42 = Add(t.41 1)
  k = t.42
  while3_top:
  t.43 = [this+8]
  t.44 = LtS(j t.43)
  if0 t.44 goto :while3_end
  t.45 = MulS(2 j)
  aux01 = t.45
  t.46 = Sub(k 3)
  aux02 = t.46
  t.47 = [this+4]
  if t.47 goto :null4
  Error("null pointer")
  null4:
  t.48 = [t.47]
  t.48 = Lt(j t.48)
  if t.48 goto :bounds3
  Error("array index out of bounds")
  bounds3:
  t.48 = MulS(j 4)
  t.48 = Add(t.48 t.47)
  t.49 = Add(aux01 aux02)
  [t.48+4] = t.49
  t.50 = Add(j 1)
  j = t.50
  t.51 = Sub(k 1)
  k = t.51
  goto :while3_top
  while3_end:
  ret 0

const functable_LS
  :LS_Start
  :LS_Print
  :LS_Search
  :LS_Init

func AllocArray(size)
  bytes = MulS(size 4)
  bytes = Add(bytes 4)
  v = HeapAllocZ(bytes)
  [v] = size
  ret v

