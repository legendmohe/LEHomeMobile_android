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

package my.home.common.util;

/**
 * Created by legendmohe on 15/11/18.
 */
public class AudioUtils {

    private static final float MAX_REPORTABLE_DB = 90.3087f;
    private static final float MAX_REPORTABLE_AMP = 32767f;


    private static double getRawAmplitude(byte[] data, int len) {
        if (len <= 0 || data == null || data.length <= 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum += Math.abs(data[i]);
        }
        return sum / len;
    }

    private static double getRawAmplitude(short[] data, int len) {
        if (len <= 0 || data == null || data.length <= 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < len; i++) {
            double sample = data[i] / 32768.0;
            sum += sample * sample;
        }
        return Math.sqrt(sum / len);
    }

    public static double getAmplitude(byte[] data, int len) {
        return (MAX_REPORTABLE_DB + (20 * Math.log10(getRawAmplitude(data, len) / MAX_REPORTABLE_AMP)));
    }

    public static double getAmplitude(short[] data, int len) {
        return (MAX_REPORTABLE_DB + 20 * Math.log10(getRawAmplitude(data, len)));
    }

    public static class FFT {

        int n, m;

        double[] cos;
        double[] sin;

        public FFT(int n) {
            this.n = n;
            this.m = (int) (Math.log(n) / Math.log(2));

            // Make sure n is a power of 2
            if (n != (1 << m))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos = new double[n / 2];
            sin = new double[n / 2];

            for (int i = 0; i < n / 2; i++) {
                cos[i] = Math.cos(-2 * Math.PI * i / n);
                sin[i] = Math.sin(-2 * Math.PI * i / n);
            }
        }

        public int getSize() {
            return this.n;
        }

        public void fft(double[] x, double[] y) {
            int i, j, k, n1, n2, a;
            double c, s, t1, t2;

            j = 0;
            n2 = n / 2;
            for (i = 1; i < n - 1; i++) {
                n1 = n2;
                while (j >= n1) {
                    j = j - n1;
                    n1 = n1 / 2;
                }
                j = j + n1;

                if (i < j) {
                    t1 = x[i];
                    x[i] = x[j];
                    x[j] = t1;
                    t1 = y[i];
                    y[i] = y[j];
                    y[j] = t1;
                }
            }

            // FFT
            n1 = 0;
            n2 = 1;

            for (i = 0; i < m; i++) {
                n1 = n2;
                n2 = n2 + n2;
                a = 0;

                for (j = 0; j < n1; j++) {
                    c = cos[a];
                    s = sin[a];
                    a += 1 << (m - i - 1);

                    for (k = j; k < n; k = k + n2) {
                        t1 = c * x[k + n1] - s * y[k + n1];
                        t2 = s * x[k + n1] + c * y[k + n1];
                        x[k + n1] = x[k] - t1;
                        y[k + n1] = y[k] - t2;
                        x[k] = x[k] + t1;
                        y[k] = y[k] + t2;
                    }
                }
            }
        }
    }

    public static int up2int(int iint) {
        int ret = 1;
        while (ret <= iint) {
            ret = ret << 1;
        }
        return ret >> 1;
    }
}
