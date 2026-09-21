public class Integral {
    public static final int STEPS = 100000002;
    public static final int THREADS = 6;
    public static final int ITEMS_PER_THREAD = STEPS / THREADS;

    public static double f(double x) {
        return x * x;
    }

    public static double g(double x) {
        return Math.sin(x) * Math.exp(x) / Math.log(x + 2);
    }

    public static Thread taskThread(int n, int[] schedule, double a, double h, double[] results, int func) {
        return new Thread(() -> {
            var start = schedule[n];
            var finish = schedule[n] + ITEMS_PER_THREAD;
            var acc = 0.0;

            if (func == 0) {
                for (int i = start; i < finish; i++) {
                    acc += f(a + (i + 0.5) * h) * h;
                }
            } else {
                for (int i = start; i < finish; i++) {
                    acc += g(a + (i + 0.5) * h) * h;
                }
            }

            results[n] = acc;
        });
    }

    public static double measureSequential(int func, double a, double b) {
        var h = (b - a) / STEPS;
        var start = System.nanoTime();
        var acc = 0.0;

        if (func == 0) {
            for (int i = 0; i < STEPS; i++) {
                acc += f(a + (i + 0.5) * h) * h;
            }
        } else {
            for (int i = 0; i < STEPS; i++) {
                acc += g(a + (i + 0.5) * h) * h;
            }
        }

        var finish = System.nanoTime();
        System.out.println("Sequential result");
        System.out.println(acc);
        System.out.println("Sequential time (ms)");
        var ms = (double) (finish - start) / 1000000;
        System.out.println(ms);
        return ms;
    }

    public static double measureParallel(int func, double a, double b) throws InterruptedException {
        var h = (b - a) / STEPS;

        var threadsStart = new int[THREADS];
        var threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = i * ITEMS_PER_THREAD;
        }

        double[] results = new double[THREADS];

        var pStart = System.nanoTime();

        for (int i = 0; i < THREADS; i++) {
            threads[i] = taskThread(i, threadsStart, a, h, results, func);
        }

        for (int i = 0; i < THREADS; i++)
            threads[i].start();

        for (int i = 0; i < THREADS; i++)
            threads[i].join();

        var pResult = 0.0;
        for (int i = 0; i < THREADS; i++) {
            pResult += results[i];
        }

        var pFinish = System.nanoTime();
        System.out.println("Parallel result");
        System.out.println(pResult);
        System.out.println("Parallel time (ms)");
        var ms = (double) (pFinish - pStart) / 1000000;
        System.out.println(ms);
        return ms;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("f(x) = x^2 on [0, 1] (exact integral = 1/3)");
        var seq = measureSequential(0, 0.0, 1.0);
        var par = measureParallel(0, 0.0, 1.0);
        System.out.println("Speedup");
        System.out.println(seq / par);

        System.out.println("g(x) = sin(x)*exp(x)/log(x+2) on [0, 2]");
        var seq2 = measureSequential(1, 0.0, 2.0);
        var par2 = measureParallel(1, 0.0, 2.0);
        System.out.println("Speedup");
        System.out.println(seq2 / par2);
    }
}
