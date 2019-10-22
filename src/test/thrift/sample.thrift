namespace java com.mamba.benchmark.thrift.sample.face

struct SharedStructIn {
  1: i32 key
  2: string value
}

struct SharedStructOut {
  1: i64 key
  2: string value
}

service SharedService {

  list<SharedStructOut> getStruct(1: i32 key, 2: string token, 3: SharedStructIn input)

  void getStruct1(1: i32 key, 2: string token, 3: SharedStructIn input)
}