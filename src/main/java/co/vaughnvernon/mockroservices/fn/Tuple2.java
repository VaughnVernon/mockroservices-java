//   Copyright © 2017-2022 Vaughn Vernon. All rights reserved.
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

package co.vaughnvernon.mockroservices.fn;

public class Tuple2<A, B> {
  public final A _1;
  public final B _2;
  
  public static <A, B> Tuple2<A, B> from(final A a, final B b) {
    return new Tuple2<A, B>(a, b);
  }

  private Tuple2(final A a, final B b) {
    this._1 = a;
    this._2 = b;
  }
}
