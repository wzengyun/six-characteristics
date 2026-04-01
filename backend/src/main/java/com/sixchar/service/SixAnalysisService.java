package com.sixchar.service;

import com.sixchar.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 六性核心算法引擎
 * 严格遵循国军标:
 * - GJB 450A-2020 可靠性维修性参数与指标
 * - GJB 899A-2009 可靠性鉴定与验收试验
 * - GJB 2072-2006 维修性工作
 * - GJB 2547A-2012 测试性工作
 * - GJB 3847A-2018 保障性工作
 * - GJB 150A-2009 环境试验
 * - GJB 900-2018 安全性工作
 */
@Service
public class SixAnalysisService {

    // ======== GJB 450A / GJB 899A 可靠性核心算法 ========

    /**
     * 指数分布 MTBF 点估计（无替换定时截尾）
     * GJB 899A: θ̂ = T / r
     */
    public double mtbfEstimate(double totalTime, int failureCount) {
        if (failureCount <= 0) return totalTime;
        return totalTime / failureCount;
    }

    /**
     * MTBF 单侧置信下限（经典方法，GJB 899A）
     * θ_L = 2T / χ²(α; 2r+2)
     */
    public double mtbfLowerLimit(double totalTime, int failureCount, double confidence) {
        if (failureCount <= 0) return totalTime;
        double alpha = 1 - confidence;
        int df = 2 * failureCount + 2;
        double chi2 = chiSquareQuantile(1 - alpha, df);
        return 2 * totalTime / chi2;
    }

    /**
     * MTBF 单侧置信上限
     * θ_U = 2T / χ²(β; 2r)
     */
    public double mtbfUpperLimit(double totalTime, int failureCount, double confidence) {
        if (failureCount <= 0) return Double.MAX_VALUE;
        double beta = 1 - confidence;
        int df = 2 * failureCount;
        if (df <= 0) return Double.MAX_VALUE;
        double chi2 = chiSquareQuantile(beta, df);
        return 2 * totalTime / chi2;
    }

    /**
     * 可靠度 R(t) = exp(-t/MTBF)
     */
    public double reliabilityAt(double mtbf, double missionTime) {
        if (mtbf <= 0) return 0;
        return Math.exp(-missionTime / mtbf);
    }

    /**
     * 可靠度置信下限（基于 MTBF_U）
     */
    public double reliabilityLowerLimit(double mtbfUpperLimit, double missionTime) {
        if (mtbfUpperLimit <= 0) return 0;
        return Math.exp(-missionTime / mtbfUpperLimit);
    }

    /**
     * GJB 899A 可靠性统计试验方案计算
     * @param planType 方案编号 "I"-"XIV"
     * @param mtbf0 MTBF 假设上限（设计目标）
     * @param mtbf1 MTBF 假设下限（最低可接受值）
     */
    public Map<String, Object> reliabilityTestPlan(String planType, double mtbf0, double mtbf1) {
        Map<String, Object> result = new LinkedHashMap<>();
        double gamma = mtbf0 / mtbf1;

        // GJB 899A Table A1 标准方案
        Map<String, double[]> plans = new LinkedHashMap<>();
        plans.put("I",    new double[]{0.20, 0.20, 1.50,  9.0,  3});
        plans.put("II",   new double[]{0.20, 0.20, 1.50, 18.5,  6});
        plans.put("III",  new double[]{0.20, 0.20, 1.50, 30.0,  9});
        plans.put("IV",   new double[]{0.20, 0.20, 1.50, 45.5, 14});
        plans.put("V",    new double[]{0.20, 0.20, 1.50, 65.5, 20});
        plans.put("VI",   new double[]{0.30, 0.30, 1.50,  4.3,  1});
        plans.put("VII",  new double[]{0.30, 0.30, 1.50,  7.8,  2});
        plans.put("VIII", new double[]{0.30, 0.30, 1.50, 12.5,  3});
        plans.put("IX",   new double[]{0.30, 0.30, 1.50, 18.2,  5});
        plans.put("X",    new double[]{0.30, 0.30, 1.50, 25.3,  7});
        plans.put("XI",   new double[]{0.20, 0.20, 2.00,  5.4,  2});
        plans.put("XII",  new double[]{0.20, 0.20, 2.00,  9.9,  3});
        plans.put("XIII", new double[]{0.20, 0.20, 3.00,  4.3,  2});
        plans.put("XIV",  new double[]{0.20, 0.20, 3.00,  7.3,  3});

        double[] p = plans.getOrDefault(planType, plans.get("X"));

        double n = p[3];   // n 倍数
        double r = p[4];    // 允许故障数

        double totalTestTime = n * mtbf1;

        result.put("planType", planType);
        result.put("producerRisk", p[0]);
        result.put("consumerRisk", p[1]);
        result.put("planRatio", p[2]);
        result.put("discriminationRatio", Math.round(gamma * 100.0) / 100.0);
        result.put("multiplier", n);
        result.put("maxFailures", (int) r);
        result.put("totalTestTimeHours", Math.round(totalTestTime));
        result.put("decisionTimeHours", Math.round(totalTestTime));
        result.put("mtbf0", mtbf0);
        result.put("mtbf1", mtbf1);

        StringBuilder rule = new StringBuilder();
        rule.append("GJB 899A-2009 试验方案").append(planType).append(":\n");
        rule.append("  鉴别比 γ = ").append(String.format("%.2f", gamma)).append("\n");
        rule.append("  允许故障数 r = ").append((int) r).append("\n");
        rule.append("  总试验时间 T = ").append(String.format("%.0f", totalTestTime)).append(" 台时\n");
        rule.append("  MTBF目标 = ").append(String.format("%.0f", mtbf0)).append("h\n");
        rule.append("  MTBF门槛 = ").append(String.format("%.0f", mtbf1)).append("h\n");
        rule.append("  接收条件: 故障数 ≤ ").append((int) r - 1).append("\n");
        rule.append("  拒收条件: 故障数 > ").append((int) r);
        result.put("decisionRule", rule.toString());
        result.put("passCriteria", "MTBF ≥ " + String.format("%.0f", mtbf0) + "h（故障数 ≤ " + ((int) r - 1) + "）");
        result.put("failCriteria", "MTBF < " + String.format("%.0f", mtbf1) + "h（故障数 > " + ((int) r) + "）");

        return result;
    }

    /**
     * 序贯截尾试验判定（GJB 899A 序贯试验）
     */
    public Map<String, Object> sequentialTestDecision(
            int observedFailures, double currentTime, double mtbf0, double mtbf1, double confidence) {

        Map<String, Object> result = new LinkedHashMap<>();
        double gamma = mtbf0 / mtbf1;
        double theta = Math.log(gamma);
        double alpha = 1 - confidence;

        // 截尾时间
        double ac = -Math.log(alpha) / theta;
        double ar = -Math.log(alpha) / theta * (1 + 0.5 / theta);
        double continueTime = Math.ceil((ac + ar) / theta * mtbf1);

        double acceptLine = Math.floor(theta * currentTime / mtbf1 - ac);
        double rejectLine = (int) Math.ceil(theta * currentTime / mtbf1 + ar);

        String decision;
        if (observedFailures <= acceptLine) {
            decision = "接收 — MTBF ≥ " + String.format("%.0f", mtbf0) + "h";
        } else if (observedFailures >= rejectLine) {
            decision = "拒收 — MTBF < " + String.format("%.0f", mtbf1) + "h";
        } else {
            decision = "继续试验（还须 " + Math.max(0, (int) (continueTime - currentTime)) + " 台时）";
        }

        result.put("observedFailures", observedFailures);
        result.put("currentTimeHours", currentTime);
        result.put("acceptLine", acceptLine);
        result.put("rejectLine", rejectLine);
        result.put("continueTimeHours", continueTime);
        result.put("decision", decision);
        result.put("mtbf0", mtbf0);
        result.put("mtbf1", mtbf1);
        result.put("gamma", Math.round(gamma * 100.0) / 100.0);
        result.put("producerRisk", alpha);

        return result;
    }

    /**
     * 威布尔分布 MTBF（位置参数 γ=0）
     * E[T] = η · Γ(1 + 1/β)
     */
    public double weibullMtbf(double eta, double beta) {
        if (eta <= 0 || beta <= 0) return 0;
        return eta * gamma(1.0 + 1.0 / beta);
    }

    /**
     * 威布尔分布可靠度 R(t) = exp(-(t/η)^β)
     */
    public double weibullReliability(double eta, double beta, double t) {
        if (eta <= 0 || beta <= 0) return 1;
        return Math.exp(-Math.pow(t / eta, beta));
    }

    // ======== GJB 2072 / GJB 368B 维修性算法 ========

    /**
     * MTTR 点估计
     */
    public double mttrEstimate(double totalMaintainTime, int maintainCount) {
        if (maintainCount <= 0) return 0;
        return totalMaintainTime / maintainCount;
    }

    /**
     * MTTR 单侧置信上限（GJB 2072）
     * θ_U = 2T_m / χ²(α; 2r)
     */
    public double mttrUpperLimit(double totalMaintainTime, int maintainCount, double confidence) {
        if (maintainCount <= 0) return totalMaintainTime;
        double alpha = 1 - confidence;
        int df = 2 * maintainCount;
        double chi2 = chiSquareQuantile(alpha, df);
        return 2 * totalMaintainTime / chi2;
    }

    /**
     * 维修度 M(t) = 1 - exp(-t/MTTR)
     */
    public double maintainabilityFunction(double mttr, double t) {
        if (mttr <= 0) return 0;
        return 1.0 - Math.exp(-t / mttr);
    }

    /**
     * 达到目标维修度所需时间 t* = -MTTR · ln(1 - M*)
     */
    public double timeToMaintainability(double mttr, double targetMaintainability) {
        if (mttr <= 0 || targetMaintainability <= 0) return 0;
        if (targetMaintainability >= 1) return Double.MAX_VALUE;
        return -mttr * Math.log(1 - targetMaintainability);
    }

    /**
     * 维修性等级评定（GJB 2072 Table 1）
     */
    public Map<String, Object> maintainabilityLevel(double mttr) {
        Map<String, Object> result = new LinkedHashMap<>();
        String level, description;
        if (mttr <= 0.5) {
            level = "Ⅰ级"; description = "优秀 — MTTR ≤ 0.5h";
        } else if (mttr <= 1.0) {
            level = "Ⅱ级"; description = "良好 — MTTR ≤ 1h";
        } else if (mttr <= 2.0) {
            level = "Ⅲ级"; description = "中等 — MTTR ≤ 2h";
        } else if (mttr <= 4.0) {
            level = "Ⅳ级"; description = "一般 — MTTR ≤ 4h";
        } else {
            level = "Ⅴ级"; description = "较差 — MTTR > 4h";
        }
        result.put("level", level);
        result.put("description", description);
        result.put("mttrHours", mttr);
        result.put("standard", "GJB 2072-2006 Table 1");
        return result;
    }

    // ======== GJB 2547A 测试性算法 ========

    /**
     * FDR 点估计
     * FDR = 检测故障数 / 注入故障总数 × 100%
     */
    public double fdrEstimate(int detected, int total) {
        if (total <= 0) return 0;
        return (double) detected / total * 100;
    }

    /**
     * FIR 点估计
     * FIR = 隔离到≤L个单元的故障数 / 检测到故障数 × 100%
     */
    public double firEstimate(int isolated, int detected) {
        if (detected <= 0) return 0;
        return (double) isolated / detected * 100;
    }

    /**
     * FDR 单侧置信下限（GJB 2547A 附录B / 等效置信区间法）
     * FDR_L = 1 - χ²(α; 2(n - r + 1)) / (2(n - r + 1))
     * 其中 r = 检测成功故障数, n = 总注入故障数
     * 当 r = n (全部检测) 时: FDR_L = 1 - χ²(α; 2) / (2)
     */
    public double fdrLowerLimit(int detected, int total, double confidence) {
        if (total <= 0) return 0;
        if (detected >= total) return 100.0; // 全部检测时置信下限为100%

        double alpha = 1 - confidence;
        // n - r = 未检测到的故障数（检测失败数）
        int undetected = total - detected;
        // 自由度 = 2 * (n - r + 1)
        int df = 2 * (undetected + 1);
        double chi2 = chiSquareQuantile(1 - alpha, df);
        double fdrLower = Math.max(0, 1 - chi2 / (2 * (undetected + 1)));
        return fdrLower * 100;
    }

    /**
     * FIR 单侧置信下限（GJB 2547A 附录B）
     * FIR_L = 1 - χ²(α; 2(n - r + 1)) / (2(n - r + 1))
     * 其中 r = 隔离成功故障数, n = 检测到故障总数
     */
    public double firLowerLimit(int isolated, int detected, double confidence) {
        if (detected <= 0) return 0;
        if (isolated >= detected) return 100.0; // 全部隔离时置信下限为100%

        double alpha = 1 - confidence;
        int notIsolated = detected - isolated;
        int df = 2 * (notIsolated + 1);
        double chi2 = chiSquareQuantile(1 - alpha, df);
        double firLower = Math.max(0, 1 - chi2 / (2 * (notIsolated + 1)));
        return firLower * 100;
    }

    /**
     * 虚警率 FAR = 虚警次数 / (故障总数 + 虚警次数) × 100%
     */
    public double farEstimate(int falseAlarms, int totalAlarms) {
        if (totalAlarms <= 0) return 0;
        return (double) falseAlarms / totalAlarms * 100;
    }

    /**
     * 测试性一次抽样方案（GJB 2547A Table B1 简化）
     */
    public Map<String, Object> testabilitySamplePlan(double targetFdr, double thresholdFdr, double alpha, double beta) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 标准方案表（工程常用方案）
        // {n样本量, a接收判定值}
        List<int[]> standardPlans = Arrays.asList(
            new int[]{10, 1},
            new int[]{20, 2},
            new int[]{30, 3},
            new int[]{50, 4},
            new int[]{60, 5},
            new int[]{80, 6},
            new int[]{100, 8}
        );

        // 近似样本量
        double p1 = targetFdr / 100.0;
        double p2 = thresholdFdr / 100.0;
        double p_bar = (p1 + p2) / 2.0;
        double z_alpha = normalQuantile(1 - alpha);
        double z_beta = normalQuantile(1 - beta);

        double num = Math.pow(z_alpha + z_beta, 2) * p_bar * (1 - p_bar);
        double den = Math.pow(p1 - p2, 2);
        if (den < 0.001) den = 0.001;
        int n_approx = Math.max(10, (int) Math.ceil(num / den));

        int[] bestPlan = standardPlans.get(0);
        int minDiff = Integer.MAX_VALUE;
        for (int[] plan : standardPlans) {
            int diff = Math.abs(plan[0] - n_approx);
            if (diff < minDiff) { minDiff = diff; bestPlan = plan; }
        }

        int n = bestPlan[0];
        int a = bestPlan[1];
        int a_star = n - a + 1; // 拒收判定值

        result.put("sampleSize", n);
        result.put("acceptNumber", a);
        result.put("rejectNumber", a + 1);
        result.put("detectionsRequired", n);
        result.put("targetFdr", targetFdr);
        result.put("thresholdFdr", thresholdFdr);
        result.put("producerRisk", alpha);
        result.put("consumerRisk", beta);

        StringBuilder rule = new StringBuilder();
        rule.append("GJB 2547A-2012 测试性一次抽样方案:\n");
        rule.append("  样本量（检测故障数）n = ").append(n).append("\n");
        rule.append("  接收判定值 a = ").append(a).append("\n");
        rule.append("  FDR目标 = ").append(String.format("%.1f", targetFdr)).append("%\n");
        rule.append("  FDR门槛 = ").append(String.format("%.1f", thresholdFdr)).append("%\n");
        rule.append("  接收条件: 成功检测数 ≥ ").append(a).append(" / ").append(n).append("\n");
        rule.append("  拒收条件: 成功检测数 ≤ ").append(a - 1).append(" / ").append(n);
        result.put("decisionRule", rule.toString());
        result.put("passCriteria", "检测到故障数 ≥ " + a + " → FDR ≥ " + String.format("%.1f", targetFdr) + "%");
        result.put("failCriteria", "检测到故障数 < " + a + " → FDR < " + String.format("%.1f", thresholdFdr) + "%");

        return result;
    }

    public int calcTestabilityScore(Testability t) {
        if (t == null) return 80;
        double fdr = t.getFdr() != null ? t.getFdr() : 0;
        double fir = t.getFir() != null ? t.getFir() : 0;
        return (int) ((fdr + fir) / 2);
    }

    // ======== GJB 3847A 保障性算法 ========

    /**
     * 保障性综合评价指数
     * S = 资源完备度×0.30 + 备件满足率×0.25 + 人员培训率×0.25 + 资料完备度×0.20
     */
    public double supportabilityIndex(double resourceScore, double spareRate, double trainingRate, double docScore) {
        return resourceScore * 0.30 + spareRate * 0.25 + trainingRate * 0.25 + docScore * 0.20;
    }

    /**
     * 保障率 SR = 保障成功次数 / 总保障需求次数 × 100%
     */
    public double supportRate(int successCount, int totalDemand) {
        if (totalDemand <= 0) return 0;
        return (double) successCount / totalDemand * 100;
    }

    // ======== GJB 150A 环境适应性算法 ========

    /**
     * 温度适应性评分（GJB 150A）
     */
    public Map<String, Object> temperatureAdaptation(double lowTemp, double highTemp, double survivalLow, double survivalHigh) {
        Map<String, Object> result = new LinkedHashMap<>();

        double workRange = highTemp - lowTemp;
        double workScore = Math.min(50, workRange * 0.5);

        double lowMargin = lowTemp - survivalLow;
        double highMargin = survivalHigh - highTemp;
        double marginScore = Math.min(50, (lowMargin + highMargin) * 0.4);

        double total = Math.min(100, workScore + marginScore);
        String level = total >= 85 ? "优秀" : total >= 70 ? "良好" : total >= 55 ? "一般" : "较差";

        result.put("workTempRange", String.format("%.0f~%.0f℃", lowTemp, highTemp));
        result.put("survivalRange", String.format("%.0f~%.0f℃", survivalLow, survivalHigh));
        result.put("workScore", String.format("%.1f", workScore));
        result.put("marginScore", String.format("%.1f", marginScore));
        result.put("totalScore", String.format("%.1f", total));
        result.put("level", level);
        result.put("standard", "GJB 150A-2009");
        return result;
    }

    /**
     * 振动适应性评分（GJB 150A）
     */
    public Map<String, Object> vibrationAdaptation(double freqMin, double freqMax, double accel, double sweepRate) {
        Map<String, Object> result = new LinkedHashMap<>();

        double range = freqMax - freqMin;
        double freqScore = Math.min(40, range * 0.2);
        double accScore = Math.min(40, accel * 2);
        double sweepScore = sweepRate <= 1 ? 20 : sweepRate <= 2 ? 15 : Math.max(5, 20 - sweepRate * 3);

        double total = Math.min(100, freqScore + accScore + sweepScore);
        String level = total >= 85 ? "优秀" : total >= 70 ? "良好" : total >= 55 ? "一般" : "较差";

        result.put("freqRange", String.format("%.0f~%.0f Hz", freqMin, freqMax));
        result.put("acceleration", String.format("%.1f g", accel));
        result.put("sweepRate", String.format("%.1f oct/min", sweepRate));
        result.put("freqScore", String.format("%.1f", freqScore));
        result.put("accScore", String.format("%.1f", accScore));
        result.put("sweepScore", String.format("%.1f", sweepScore));
        result.put("totalScore", String.format("%.1f", total));
        result.put("level", level);
        result.put("standard", "GJB 150A-2009");
        return result;
    }

    /**
     * 湿度适应性评分
     */
    public Map<String, Object> humidityAdaptation(double minHumidity, double maxHumidity) {
        Map<String, Object> result = new LinkedHashMap<>();
        double range = maxHumidity - minHumidity;
        double score = Math.min(100, range * 0.8);
        String level = score >= 85 ? "优秀" : score >= 70 ? "良好" : score >= 55 ? "一般" : "较差";
        result.put("humidityRange", String.format("%.0f~%.0f%%RH", minHumidity, maxHumidity));
        result.put("score", String.format("%.1f", score));
        result.put("level", level);
        result.put("standard", "GJB 150A-2009");
        return result;
    }

    public double environmentOverallScore(double tempScore, double vibrationScore, double humidityScore) {
        return tempScore * 0.35 + vibrationScore * 0.35 + humidityScore * 0.30;
    }

    // ======== GJB 900 安全性算法 ========

    /**
     * 故障树顶事件失效概率
     * OR门: P = 1 - Π(1-Pi)
     * AND门: P = Π(Pi)
     */
    public double ftaTopProbability(double[] probs, String gateType) {
        if (probs == null || probs.length == 0) return 0;
        if ("AND".equalsIgnoreCase(gateType)) {
            double r = 1.0;
            for (double p : probs) r *= p;
            return Math.min(r, 1.0);
        } else {
            double r = 1.0;
            for (double p : probs) r *= (1 - p);
            return Math.min(1 - r, 1.0);
        }
    }

    /**
     * 安全完整性等级（GJB 900 / IEC 61508）
     */
    public Map<String, Object> safetyIntegrityLevel(double pfd) {
        Map<String, Object> result = new LinkedHashMap<>();
        String sil;
        if (pfd <= 1e-4) sil = "SIL 4（最高完整性）";
        else if (pfd <= 1e-3) sil = "SIL 3（高完整性）";
        else if (pfd <= 1e-2) sil = "SIL 2（中等完整性）";
        else if (pfd <= 1e-1) sil = "SIL 1（低完整性）";
        else sil = "未达到 SIL 1（不可接受）";

        result.put("sil", sil);
        result.put("pfp", String.format("%.2e", pfd));
        result.put("standard", "GJB 900-2018 / IEC 61508");
        return result;
    }

    /**
     * 安全性评分（满分100）
     */
    public int calcSafetyScore(Safety sf) {
        if (sf == null || sf.getFailureProb() == null) return 80;
        double pf = sf.getFailureProb();
        if (pf <= 1e-5) return 100;
        if (pf <= 1e-4) return 90;
        if (pf <= 1e-3) return 75;
        if (pf <= 1e-2) return 60;
        return 40;
    }

    // ======== 综合评分 ========

    public int calcReliabilityScore(Reliability r) {
        if (r == null || r.getMtbf() == null || r.getMtbf() <= 0) return 85;
        double mtbf = r.getMtbf();
        if (mtbf >= 5000) return 100;
        if (mtbf >= 3000) return 92;
        if (mtbf >= 2000) return 85;
        if (mtbf >= 1000) return 75;
        if (mtbf >= 500)  return 60;
        return 45;
    }

    public int calcMaintainabilityScore(Maintainability m) {
        if (m == null || m.getMttr() == null) return 82;
        double mttr = m.getMttr();
        if (mttr <= 0.5) return 100;
        if (mttr <= 1.0) return 95;
        if (mttr <= 2.0) return 85;
        if (mttr <= 4.0) return 72;
        if (mttr <= 8.0) return 58;
        return 40;
    }

    public int calcSupportabilityScore(Supportability s) {
        if (s == null || s.getSupportRate() == null) return 85;
        return s.getSupportRate().intValue();
    }

    public int calcEnvironmentScore(Environment env) {
        if (env == null || env.getAdaptScore() == null) return 83;
        return env.getAdaptScore().intValue();
    }

    public double calcOverallScore(
            Reliability r, Maintainability m, Testability t,
            Supportability sp, Safety sf, Environment env) {
        int[] scores = new int[6];
        scores[0] = calcReliabilityScore(r);
        scores[1] = calcMaintainabilityScore(m);
        scores[2] = calcTestabilityScore(t);
        scores[3] = calcSupportabilityScore(sp);
        scores[4] = calcSafetyScore(sf);
        scores[5] = calcEnvironmentScore(env);
        int sum = 0;
        for (int s : scores) sum += s;
        return (double) sum / scores.length;
    }

    // ======== 数学工具函数 ========

    /**
     * 卡方分布分位数近似（Wilson-Hilferty 变换）
     */
    private double chiSquareQuantile(double p, int df) {
        if (df <= 0) return 0;
        if (df == 1) {
            if (p <= 0.001)  return 0.0000;
            if (p <= 0.01)   return 0.0002;
            if (p <= 0.025)  return 0.00098;
            if (p <= 0.05)   return 0.00393;
            if (p <= 0.1)    return 0.0158;
            if (p <= 0.5)    return 0.4549;
            if (p <= 0.9)    return 2.706;
            if (p <= 0.95)   return 3.841;
            if (p <= 0.975)  return 5.024;
            if (p <= 0.99)   return 6.635;
            if (p <= 0.999)  return 10.828;
            return 0;
        }
        if (df == 2) {
            if (p <= 0.1)   return 0.210;
            if (p <= 0.5)    return 1.386;
            if (p <= 0.9)    return 4.605;
            if (p <= 0.95)  return 5.991;
            if (p <= 0.99)   return 9.210;
            return 0;
        }
        // Wilson-Hilferty 近似
        double z = normalQuantile(p);
        double term1 = 2.0 / (9.0 * df);
        double term2 = z * Math.sqrt(term1);
        double term3 = 1 - term1;
        double raw = df * Math.pow(term3 + term2, 3);
        return Math.max(raw, 0.001);
    }

    /**
     * 标准正态分布分位数（近似，Horner 方法）
     */
    private double normalQuantile(double p) {
        if (p <= 0) return -8;
        if (p >= 1) return 8;
        if (p <= 0.5) {
            double t = Math.sqrt(-2 * Math.log(p));
            return -(t - (0.010328 * t + 0.802853) * t + 2.515517) /
                   ((0.001308 * t + 0.189269) * t + 1.432788) + t;
        } else {
            return -normalQuantile(1 - p);
        }
    }

    /**
     * Gamma 函数近似（ Lanczos 方法简化）
     * Γ(x) ≈ √(2π/x) · (x + 1/(12x - 1/(10x))) ^ x · e^-x
     */
    private double gamma(double x) {
        if (x <= 0) return 1;
        double g = 7;
        double[] c = {0.99999999999980993, 676.5203681218851, -1259.1392167224028,
                      771.32342877765313, -176.61502916214059, 12.507343278686905,
                      -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7};
        if (x < 0.5) {
            return Math.PI / (Math.sin(Math.PI * x) * gamma(1 - x));
        }
        x -= 1;
        double a = c[0];
        for (int i = 1; i < g + 2; i++) {
            a += c[i] / (x + i);
        }
        double t = x + g + 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, x + 0.5) * Math.exp(-t) * a;
    }
}
