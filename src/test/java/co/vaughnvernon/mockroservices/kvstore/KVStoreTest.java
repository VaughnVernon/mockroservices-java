//   Copyright © 2017 Vaughn Vernon. All rights reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package co.vaughnvernon.mockroservices.kvstore;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KVStoreTest {

  @Test
  public void testPutGet() throws Exception {
    final String key = "k1";
    final String value = "v1";
    
    final String name = "test";
    final KVStore store = KVStore.open(name);
    
    store.put(key, value);
    
    assertEquals(name, store.name);
    assertEquals(value, store.get(key));
  }
}
