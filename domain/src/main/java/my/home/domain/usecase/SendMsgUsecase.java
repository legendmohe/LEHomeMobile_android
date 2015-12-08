
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.domain.usecase;

import java.io.File;

/**
 * Created by legendmohe on 15/12/1.
 */
public interface SendMsgUsecase extends Usecase {

    String TAG = "SendMsgUsecase";

    enum Event {
        START(0), FINISH(1), CANCEL(2), ERROR(3);

        private int value;

        Event(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }
    }

    SendMsgUsecase setTargetFile(File targetFile);

    SendMsgUsecase setEvent(Event event);

    void cleanup();

    void cancel(File file);
}
