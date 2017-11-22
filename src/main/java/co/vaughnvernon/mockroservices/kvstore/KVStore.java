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

import java.util.HashMap;
import java.util.Map;

public class KVStore {
  private static final Map<String, KVStore> stores = new HashMap<>();
  
  public final String name;
  private final Map<String, String> store;
  
  public static KVStore open(final String name) {
    final KVStore openStore = stores.get(name);
    
    if (openStore != null) {
      return openStore;
    }
    
    final KVStore store = new KVStore(name);
    
    stores.put(name, store);
    
    return store;
  }
  
  public String get(final String key) {
    return store.get(key);
  }

  public void put(final String key, final String value) {
    store.put(key, value);
  }
  
  private KVStore(final String name) {
    this.name = name;
    this.store = new HashMap<>();
  }
}
