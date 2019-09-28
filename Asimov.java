/******************************************************************************
 *  Compilação:  javac Asimov.java
 *  Execução:    java Asimov at n 600            (n random particles for 600 't's)
 *                java Asimov pc < input.txt     (from a file)
 *                java Asimov rc s < input.txt   (from a file of type 's')
 *
 *  Dependencies: StdDraw.java Particle.java MinPQ.java
 *
 *  Creates n random particles and simulates their motion according
 *  to the laws of elastic collisions.
 ******************************************************************************/

import java.awt.Color;
import java.util.ArrayList;

public class Asimov {

	// Área com variáveis globais.

    private MinPQ<Event> pq;          		 // Fila de prioridades.
    private double t;          				 // Relógio da simulação.
    private static final double HZ = 0.5;    // Número de eventos feitos a cada tick do relógio.
    private Particle[] particles;     		 // Array que contém as partículas a serem usadas.

	// Seção do código aproveitada de 'CollisionSystem.java' -------------------------*/

    // Fazemos isso, pois, quando usávamos o método '.clone()', não estávamos fazendo uma
    // cópia, só estávamos colocando ponteiros que apontavam também para particles. Deixamos
    // o segundo modo, pois nossas simulações que avançam com um pequeno 'dt' se beneficiariam
    // de ter seus dados originais alterados, pois não teríamos que sempre copiar todas as informações
    // várias vezes.
    public Asimov(Particle[] particles) {
        this.particles = new Particle[particles.length]; // Cópia defensiva.
        for (int i = 0; i < particles.length; i++) this.particles[i] = new Particle(particles[i].rx(), particles[i].ry(), particles[i].vx(), particles[i].vy(), particles[i].radius(), particles[i].mass(), particles[i].color());
    }

    // Atualiza a fila de prioridades, agora com todos os novos eventos
    // relacionados à 'a'.
    private void predict(Particle a, double limit) {
        if (a == null) return;

        for (int i = 0; i < particles.length; i++) {
            double dt = a.timeToHit(particles[i]);
            if (t + dt <= limit)
                pq.insert(new Event(t + dt, a, particles[i]));
        }

        double dtX = a.timeToHitVerticalWall();
        double dtY = a.timeToHitHorizontalWall();
        if (t + dtX <= limit) pq.insert(new Event(t + dtX, a, null));
        if (t + dtY <= limit) pq.insert(new Event(t + dtY, null, a));
    }

    // Redesenha todas partículas.
    private void redraw(double limit) {
        StdDraw.clear();
        for (int i = 0; i < particles.length; i++) particles[i].draw();
        StdDraw.show();
        StdDraw.pause(20);
        if (t < limit) pq.insert(new Event(t + 1.0 / HZ, null, null));
    }

    private static class Event implements Comparable<Event> {
        private final double time;         // time that event is scheduled to occur
        private final Particle a, b;       // particles involved in event, possibly null
        private final int countA, countB;  // collision counts at event creation


        // create a new event to occur at time t involving a and b
        public Event(double t, Particle a, Particle b) {
            this.time = t;
            this.a    = a;
            this.b    = b;
            if (a != null) countA = a.count();
            else           countA = -1;
            if (b != null) countB = b.count();
            else           countB = -1;
        }

        // compare times when two events will occur
        public int compareTo(Event that) {
            return Double.compare(this.time, that.time);
        }

        // has any collision occurred between when event was created and now?
        public boolean isValid() {
            if (a != null && a.count() != countA) return false;
            if (b != null && b.count() != countB) return false;
            return true;
        }

    }

    /*----------------------------------------------------------------------------------*/

    // Parte original deste código.

    // Método tanto para efeito borboleta, quanto divergência e funções do tipo gráfico */

    // Vê a diferença média entre duas configurações. Esta função é usada tanto em 'borboleta'
    // quanto em 'diverge'.
    public static double dif (double[][] p, double[][] p2) {
        double dif = 0.0;
        for (int i = 0; i < p.length; i++)
            // Soma a distância entre as duas partículas.
            dif += Math.sqrt((p[i][0]-p2[i][0])*(p[i][0]-p2[i][0]) + (p[i][1]-p2[i][1])*(p[i][1]-p2[i][1]));
        dif = dif/p.length;
        return dif;
    }

    /*-----------------------------------------------------------------------------------*/

    // Simulações padrão ---------------------------------------------------------------*/

    // Simulação que ocorre por um tempo determinado pelo clique do usuário,
    public void simulaClique (double limite) {

        t = 0.0;
        pq = new MinPQ<Event>();
        for (int i = 0; i < particles.length; i++) predict(particles[i], limite);
        pq.insert(new Event(0, null, null));
        boolean bool = true;

        // Laço principal da simulação
        while ((bool) && (!pq.isEmpty())) {
            Event e = pq.delMin();
            if (!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;
            for (int i = 0; i < particles.length; i++) particles[i].move(e.time - t);
            t = e.time;
            if      (a != null && b != null) a.bounceOff(b);
            else if (a != null && b == null) a.bounceOffVerticalWall();
            else if (a == null && b != null) b.bounceOffHorizontalWall();
            else if (a == null && b == null) redraw(limite);
            predict(a, limite);
            predict(b, limite);
            // Quando o usuário clica começamos o processo de reversão.
            if (StdDraw.isMousePressed()) bool = false;
        }
    }

    // Simulação que ocorre por um tempo determinado pelo usuário, esse tempo é medido
    // em relação À variável global 't'.
    public void simulaTempo (double limite, double tempo) {

    	t = 0.0;
    	pq = new MinPQ<Event>();
        for (int i = 0; i < particles.length; i++) predict(particles[i], limite);
        pq.insert(new Event(0, null, null));

    	while ((t < tempo) && (!pq.isEmpty())) {
            Event e = pq.delMin();
            if (!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;
            for (int i = 0; i < particles.length; i++) particles[i].move(e.time - t);
            t = e.time;
            if      (a != null && b != null) a.bounceOff(b);
            else if (a != null && b == null) a.bounceOffVerticalWall();
            else if (a == null && b != null) b.bounceOffHorizontalWall();
            else if (a == null && b == null) redraw(limite);
            predict(a, limite);
            predict(b, limite);
        }
    }

    public ArrayList<Double> simulaGrafico (double limite, double tempo, double time, double[][] poso) {

        t = 0.0;
        pq = new MinPQ<Event>();
        for (int i = 0; i < particles.length; i++) predict(particles[i], limite);
        pq.insert(new Event(0, null, null));

        double[][] pos = new double[particles.length][2];
        ArrayList<Double> dif = new ArrayList<Double>();
        int j = 0;

        while ((t < tempo) && (!pq.isEmpty())) {
            Event e = pq.delMin();
            if (!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;
            for (int i = 0; i < particles.length; i++) particles[i].move(e.time - t);
            t = e.time;
            if      (a != null && b != null) a.bounceOff(b);
            else if (a != null && b == null) a.bounceOffVerticalWall();
            else if (a == null && b != null) b.bounceOffHorizontalWall();
            predict(a, limite);
            predict(b, limite);

            for (int i = 0; i < particles.length; i++) {
                pos[i][0] = particles[i].rx();
                pos[i][1] = particles[i].ry();
            }
            dif.add(j, dif(poso, pos));
            j++;
        }

        return dif;
    }

    // Roda uma simulação sem printar o que está acontecendo.
    public void simulaSemPrint (double limite, double tempo) {

        t = 0.0;
        pq = new MinPQ<Event>();
        for (int i = 0; i < particles.length; i++) predict(particles[i], limite);
        pq.insert(new Event(0, null, null));

        while ((t < tempo) && (!pq.isEmpty())) {
            Event e = pq.delMin();
            if (!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;
            for (int i = 0; i < particles.length; i++) particles[i].move(e.time - t);
            t = e.time;
            if      (a != null && b != null) a.bounceOff(b);
            else if (a != null && b == null) a.bounceOffVerticalWall();
            else if (a == null && b != null) b.bounceOffHorizontalWall();
            //else if (a == null && b == null) redraw(limite);
            predict(a, limite);
            predict(b, limite);
        }
    }

    // Comanda o tipo de simulação no caso em que o usuário clica para determinar o tempo.
    public void simulateClique(double limite) {

        simulaClique(limite);
        double tempo = t;
        // Breve pausa, para que o estado atual possa ser analisado.
        StdDraw.pause(1500);
        for (int i = 0; i < particles.length; i++) particles[i].inverteVel();
        simulaTempo(limite, tempo);
    }

    // Função muito semelhante a de cima, mas esta tem o tempo determinado como argumento.
    public void simulateTempo(double limite, double tempo) {

    	// Simula o código como 'CollisionSystem.java'.
        simulaTempo(limite, tempo);
        // Simula o retrocesso.
        StdDraw.pause(1500);
        for (int i = 0; i < particles.length; i++) particles[i].inverteVel();
        simulaTempo(limite, tempo);
    }

    // Simulação do tipo que faz um gráfico e printa a distância média do estado
    // atual para o inicial.
    public double[] simulateGrafico(double limite, double tempo) {

        // Guarda as posições iniciais.
        double[][] poso = new double[particles.length][2];
        for (int i = 0; i < particles.length; i++) {
            poso[i][0] = particles[i].rx();
            poso[i][1] = particles[i].ry();
        }

        ArrayList<Double> temp = simulaGrafico(limite, tempo, 0.0, poso);
        for (int i = 0; i < particles.length; i++) particles[i].inverteVel();
        ArrayList<Double> temp2 = simulaGrafico(limite, tempo, tempo, poso);
        double[] dists = new double[temp.size() + temp2.size() + 1];
        int j = 0;
        for (Double d : temp) dists[j++] = d;
        for (Double d : temp2) dists[j++] = d;

        return dists;
    }

    /*-----------------------------------------------------------------------------------*/

    // Chamadas padrão ------------------------------------------------------------------*/

    // Quando as funções padrões são chamadas sem o argumento do tempo, elas são utilizadas
    // no modo de retrocesso por clique.

    // Baseado num modo preexistente em 'CollisionSystem.java'.
    public static void aleatorioClique(int n) {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
        Particle[] particles = new Particle[n];
		for (int i = 0; i < n; i++) particles[i] = new Particle();
    	Asimov system = new Asimov(particles);
        system.simulateClique(10000);
    }

    public static void aleatorioTempo(int n, double tempo) {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) particles[i] = new Particle();
        Asimov system = new Asimov(particles);
        system.simulateTempo(10000, tempo);
    }

    public static void aleatorioGrafico(int n, double tempo) {

        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) particles[i] = new Particle();

        Asimov system = new Asimov(particles);
        double[] dists = system.simulateGrafico(10000, tempo);

        double min = 0.0; double max = 0.0;

        for (int i = 0; i < dists.length; i++) {
            if (dists[i] > max) max = dists[i];
        }

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, dists.length - 1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < dists.length; i++) StdDraw.line(i-1, dists[i-1], i, dists[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Recebe um txt semelhante àqueles de 'CollisionSystem.java'.
    public static void padraoClique() {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
    	int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }
        Asimov system = new Asimov(particles);
        system.simulateClique(10000);
    }

    public static void padraoTempo(double tempo) {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }
        Asimov system = new Asimov(particles);
        system.simulateTempo(10000, tempo);
    }

    public static void padraoGrafico(double tempo) {

        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }

        Asimov system = new Asimov(particles);
        double[] dists = system.simulateGrafico(10000, tempo);

        double min = 0.0; double max = 0.0;

        for (int i = 0; i < dists.length; i++) {
            if (dists[i] > max) max = dists[i];
        }

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, dists.length - 1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < dists.length; i++) StdDraw.line(i-1, dists[i-1], i, dists[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Recebe um txt semelhante ao de padrão, mas sem as velocidades, de modo
    // que estas são selecionadas aleatoriamente.
    public static void randomicoClique(double divisor) {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
    	int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = Math.random()/divisor;
            double vy     = Math.random()/divisor;
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }
        Asimov system = new Asimov(particles);
        system.simulateClique(10000);
    }

    public static void randomicoTempo(double divisor, double tempo) {

        StdDraw.setCanvasSize(600,600);
        StdDraw.enableDoubleBuffering();
        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = Math.random()/divisor;
            double vy     = Math.random()/divisor;
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }
        Asimov system = new Asimov(particles);
        system.simulateTempo(10000, tempo);
    }

    public static void randomicoGrafico(double divisor, double tempo) {

        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = Math.random()/divisor;
            double vy     = Math.random()/divisor;
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }

        Asimov system = new Asimov(particles);
        double[] dists = system.simulateGrafico(10000, tempo);

        double min = 0.0; double max = 0.0;

        for (int i = 0; i < dists.length; i++) {
            if (dists[i] > max) max = dists[i];
        }

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, dists.length - 1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < dists.length; i++) StdDraw.line(i-1, dists[i-1], i, dists[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    /*-----------------------------------------------------------------------------------*/

    // Efeito Borboleta -----------------------------------------------------------------*/

    // Devolve a posição de uma partícula depois de um determinado tempo de simulação,
    // é importante notar que neste caso não fazemos o processo de retroceder.
    public double[][] posB (double limite, double tempo) {

    	double[][] pos = new double[particles.length][2];
        simulaSemPrint(limite, tempo);
    	for (int i = 0; i < particles.length; i++) {
    		pos[i][0] = particles[i].rx();
    		pos[i][1] = particles[i].ry();
    	}
    	return pos;
    }

    // É usada para fazer um número aleatório entre -1 e 1, de modo a passar o sinal.
    public static int sinal() {
		double prob = Math.random();
        int sinal = 1;
        if (sinal <= 0.5) sinal = -1;
        return sinal;
    }

    // Deixamos dois sistemas similares rodando por determinado tempo e vemos quanto tempo
    // demora para eles diferirem um determinado limiar. A constante dif determina a pequena
    // diferença que será aplicada para deixar os dois sistemas diferentes.
    public static double simulaBorboleta(Particle[] particles, Particle[] particles2, double limiar, double delta) {

        double tempoAnterior = 0.0; double tempo = 100.0;
        while (Math.abs(tempo - tempoAnterior) > delta) {

            Asimov system = new Asimov(particles);
            double[][] pos = system.posB(10000, tempo);

            Asimov system2 = new Asimov(particles2);
            double[][] pos2 = system2.posB(10000, tempo);

            double dif = dif(pos, pos2);

            if (dif > limiar) tempo = (tempoAnterior + tempo)/2;
            else {
                tempoAnterior = tempo;
                tempo = 2 * tempo;
            }
        }
        return tempo;
    }

    // Coordena a criação das duas diferentes configurações e chama a simulação.
    public static double borboleta(double limiar, double dif, double delta) {

        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        Particle[] particles2 = new Particle[n];
        for (int i = 0; i < n; i++) {

            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);

            double rx2    = rx + sinal() * dif * Math.random();
            double ry2    = ry + sinal() * dif * Math.random();
            double vx2    = vx + sinal() * dif * Math.random()/200;
            double vy2    = vy + sinal() * dif * Math.random()/200;

            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
            particles2[i] = new Particle(rx2, ry2, vx2, vy2, radius, mass, color);
        }

        double t = simulaBorboleta(particles, particles2, limiar, delta);
        return t;
    }

    // Mesma coisa, mas criado aleatoriamente
    public static double borboletaAleatoria(int n, double limiar, double dif, double delta) {

        Particle[] particles = new Particle[n];
        Particle[] particles2 = new Particle[n];

        for (int i = 0; i < n; i++) {

            double rx     = Math.random();
            double ry     = Math.random();
            double vx     = Math.random()/200;
            double vy     = Math.random()/200;
            double radius = 0.02;
            double mass   = 0.5;
            Color color  = Color.BLACK;

            double rx2    = rx + sinal() * dif * Math.random();
            double ry2    = ry + sinal() * dif * Math.random();
            double vx2    = vx + sinal() * dif * Math.random()/200;
            double vy2    = vy + sinal() * dif * Math.random()/200;

            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
            particles2[i] = new Particle(rx2, ry2, vx2, vy2, radius, mass, color);
        }

        double t = simulaBorboleta(particles, particles2, limiar, delta);
        return t;
    }

    // Analisamos os tempos que demoram para duas configurações diferirem com base em determinados limiares.
    public static void borboletaLimiar (int N, double limiar, double dif, double fator, double delta) {

        int n = StdIn.readInt();

        Particle[] particles = new Particle[n];
        Particle[] particles2 = new Particle[n];

        // Cria configurações fixas, de forma que só o limiar se altera na execução.
        for (int i = 0; i < n; i ++) {
            double rx     = StdIn.readDouble();
            double rx2    = rx + sinal() * dif * Math.random();

            double ry     = StdIn.readDouble();
            double ry2    = ry + sinal() * dif * Math.random();

            double vx     = StdIn.readDouble();
            double vx2    = vx + sinal() * dif * Math.random()/200;

            double vy     = StdIn.readDouble();
            double vy2    = vy + sinal() * dif * Math.random()/200;

            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
            particles2[i] = new Particle(rx2, ry2, vx2, vy2, radius, mass, color);
        }

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = simulaBorboleta(particles, particles2, limiar, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
            limiar *= fator;
        }

        double min = tempos[0]; double max = tempos[N-1];

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Versão equivalente à de cima, mas sem entrada especificada.
    public static void borboletaLimiarAleatorio (int N, int n, double limiar, double dif, double fator, double delta) {

        Particle[] particles = new Particle[n];
        Particle[] particles2 = new Particle[n];

        // Cria configurações aleatórias, de forma que só o limiar vai alterar durante a execução.
        for (int i = 0; i < n; i++) {

            double rx     = Math.random();
            double ry     = Math.random();
            double vx     = Math.random()/200;
            double vy     = Math.random()/200;
            double radius = 0.02;
            double mass   = 0.5;
            Color color  = Color.BLACK;

            double rx2    = rx + sinal() * dif * Math.random();
            double ry2    = ry + sinal() * dif * Math.random();
            double vx2    = vx + sinal() * dif * Math.random()/200;
            double vy2    = vy + sinal() * dif * Math.random()/200;

            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
            particles2[i] = new Particle(rx2, ry2, vx2, vy2, radius, mass, color);
        }

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = simulaBorboleta(particles, particles2, limiar, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
            limiar *= fator;
        }

        double min = tempos[0]; double max = tempos[N-1];

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Cria N configurações aleatórias e para cada uma delas uma outra configuração pouco diferente.
    // Com isso analisamos os tempos que demoram para elas diferirem baseados em determinado limiar.
    public static void borboletaN (int N, int n, double limiar, double dif, double delta) {

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = borboletaAleatoria(n, limiar, dif, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
        }

        double max = tempos[0]; double min = tempos[0];

        for (int i = 0; i < N; i++) {
            if (tempos[i] > max) max = tempos[i];
            else if (tempos[i] < min) min = tempos[i];
        }

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    /*-----------------------------------------------------------------------------------*/

    // Códigos que analisam divergência --------------------------------------------------*/

    // Simula uma partícula e a retrocede, depois guarda as suas posições.
    public double[][] posD (double limite, double tempo) {

    	simulaSemPrint(limite, tempo);
        for (int i = 0; i < particles.length; i++) particles[i].inverteVel();
        simulaSemPrint(limite, tempo);
    	double[][] pos = new double[particles.length][2];
    	for (int i = 0; i < particles.length; i++) {
    		pos[i][0] = particles[i].rx();
    		pos[i][1] = particles[i].ry();
    	}
    	return pos;
    }

    /*
       Guarda as posições inciais da partícula e depois a simula por um determinado tempo
       depois vê se essas posições diferem por um caráter maior ou menor que um dado épisilon.
       Isso é feito de maneira a dobrar e dividir por dois o tempo, de modo que num processo
       semelhante ao da busca binária, vamos obter o tempo.

       Uma coisa interessante é que 'poso' era criado nesse código, mas vimos que a função 'divergeEps'
       ia utilizar de modo intensivo a posição inicial, então, em troca de um código mais limpo e rápido
       naquela função, fizemos com que cada função diverge tivesse que criar suas prórpias 'poso', o que
       fez as demais ficarem menos organizadas, mas foi a troca necessária para salvar tempo e não deixar
       a função 'divergeEps' tão desorganizada.
    */
    public static double simulaDiverge(Particle[] particles, double[][] poso, double tempoAnterior, double tempo, double eps, double delta) {

        while (Math.abs(tempo - tempoAnterior) > delta) {
        	Asimov system = new Asimov(particles);
        	double[][] pos = system.posD(10000, tempo);
        	double dif = dif(poso, pos);
        	// O tempo para divergir é maior que o que estamos analisando;
        	if (dif < eps) {
                tempoAnterior = tempo;
                tempo = 2 * tempo;
            }
        	else tempo = (tempo + tempoAnterior)/2;
        }
        return tempo;
    }

    // Descobre quanto tempo um sistema demora para divergir de determinado épislon,
    // depois de retroceder.
    public static double diverge(double eps, double delta) {

        int n = StdIn.readInt();
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }

        // Guarda as posições iniciais.
        double[][] poso = new double[particles.length][2];
        for (int i = 0; i < particles.length; i++) {
            poso[i][0] = particles[i].rx();
            poso[i][1] = particles[i].ry();
        }

        double t = simulaDiverge(particles, poso, 0.0, 100.0, eps, delta);
        return t;
    }

    // Cria um sistema randômico e vê qual o tempo limite para que o sistema não retroceda à sua
    // configuração inicial, com uma margem épsilon de erro.
    public static double divergeAleatorio(int n, double eps, double delta) {

        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) particles[i] = new Particle();
        // Posições iniciais.
        double[][] poso = new double[particles.length][2];
        for (int i = 0; i < particles.length; i++) {
            poso[i][0] = particles[i].rx();
            poso[i][1] = particles[i].ry();
        }

        double t = simulaDiverge(particles, poso, 0.0, 100.0, eps, delta);
        return t;
    }

    // Semelhante à 'diverge', mas esta desenha um gráfico de tempo limite para que o
    // sistema retroceda ao estado inicial e número de iterações do for que realiza isto.
    public static void divergeEps (int N, double eps, double fator, double delta) {

        int n = StdIn.readInt();

        // Configuração fixa, para analisar o comportamento do tempo de colapso e épsilon.
        Particle[] particles = new Particle[n];

        for (int i = 0; i < n; i++) {
            double rx     = StdIn.readDouble();
            double ry     = StdIn.readDouble();
            double vx     = StdIn.readDouble();
            double vy     = StdIn.readDouble();
            double radius = StdIn.readDouble();
            double mass   = StdIn.readDouble();
            int r         = StdIn.readInt();
            int g         = StdIn.readInt();
            int b         = StdIn.readInt();
            Color color   = new Color(r, g, b);
            particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
        }

        // Posições iniciais.
        double[][] poso = new double[particles.length][2];
        for (int i = 0; i < particles.length; i++) {
            poso[i][0] = particles[i].rx();
            poso[i][1] = particles[i].ry();
        }

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = simulaDiverge(particles, poso, 0.0, 100.0, eps, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
            eps *= fator;
        }

        double min = tempos[0]; double max = tempos[N-1];

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Versão equivalente à de cima, mas sem entrada especificada.
    public static void divergeEpsAleatorio (int N, int n, double eps, double fator, double delta) {

        // Cria uma configuração aleatória, de modo que ela será fixa e só épsilon irá mudar.
        Particle[] particles = new Particle[n];
        for (int i = 0; i < n; i++) particles[i] = new Particle();

        // Posições iniciais.
        double[][] poso = new double[particles.length][2];
        for (int i = 0; i < particles.length; i++) {
            poso[i][0] = particles[i].rx();
            poso[i][1] = particles[i].ry();
        }

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = simulaDiverge(particles, poso, 0.0, 100.0, eps, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
            eps *= fator;
        }

        double min = tempos[0]; double max = tempos[N-1];

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    // Simula 'N' configurações aleatórias e vê quanto tempo demora até elas divergirem.
    // Aqui o épsilon é fixo e as configurações variam.
    public static void divergeN (int N, int n, double eps, double delta) {

        double[] tempos = new double[N];

        for (int i = 0; i < N; i++) {
            tempos[i] = divergeAleatorio(n, eps, delta);
            System.out.println("O tempo " + i + " vale: " + tempos[i]);
        }

        double max = tempos[0]; double min = tempos[0];

        for (int i = 0; i < N; i++) {
            if (tempos[i] > max) max = tempos[i];
            else if (tempos[i] < min) min = tempos[i];
        }

        if (max - min == 0.0) {
            System.out.println("Mínimo e máximo são iguais: " + min);
        }

        else {
            StdDraw.setCanvasSize(600,600);
            StdDraw.setXscale(0, N-1);
            StdDraw.setYscale(min,max);
            StdDraw.setPenRadius();
            for (int i = 1; i < N; i++) StdDraw.line(i-1, tempos[i-1], i, tempos[i]);
            System.out.println("O mínimo vale: " + min + " e o máximo vale: " + max);
        }
    }

    /*-----------------------------------------------------------------------------------*/

    public static void main(String[] args) {

    	// Guarda o modo que vamos fazer a chamada.
    	String modo = args[0];

        // Modos de análise efeito borboleta.
        if (modo.equals("b")) {System.out.println("O tempo até os dois estados divergirem é: " + borboleta(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));}
        else if (modo.equals("ba")) {System.out.println("O tempo até os dois estados divergirem é: " + borboletaAleatoria(Integer.parseInt(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])));}
        else if (modo.equals("bl")) borboletaLimiar(Integer.parseInt(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
        else if (modo.equals("bla")) borboletaLimiarAleatorio(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
        else if (modo.equals("bn")) borboletaN(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));

        // Modos de análise de comportamento divergente.
        else if (modo.equals("d")) System.out.println("O tempo até o sistema divergir vale: " + diverge(Double.parseDouble(args[1]), Double.parseDouble(args[2])));
        else if (modo.equals("da")) System.out.println("O tempo até um sistema aleatório divergir vale: " + divergeAleatorio(Integer.parseInt(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
    	  else if (modo.equals("de")) divergeEps(Integer.parseInt(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        else if (modo.equals("dea")) divergeEpsAleatorio(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
        else if (modo.equals("dn")) divergeN(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));

        // Modos baseados em cliques.
        else if (modo.equals("ac")) aleatorioClique(Integer.parseInt(args[1]));
       	else if (modo.equals("pc")) padraoClique();
      	else if (modo.equals("rc")) randomicoClique(Double.parseDouble(args[1]));

        // Modos baseados em tempo.
        else if (modo.equals("at")) aleatorioTempo(Integer.parseInt(args[1]), Double.parseDouble(args[2]));
        else if (modo.equals("pt")) padraoTempo(Double.parseDouble(args[1]));
        else if (modo.equals("rt")) randomicoTempo(Double.parseDouble(args[1]), Double.parseDouble(args[2]));

        // Modos baseados em gráficos.
        else if (modo.equals("ag")) aleatorioGrafico(Integer.parseInt(args[1]), Double.parseDouble(args[2]));
        else if (modo.equals("pg")) padraoGrafico(Double.parseDouble(args[1]));
        else if (modo.equals("rg")) randomicoGrafico(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
    }
}
