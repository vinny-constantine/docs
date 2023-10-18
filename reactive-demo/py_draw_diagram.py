from matplotlib import pyplot as plt
from matplotlib.pyplot import figure
from matplotlib import cycler
from matplotlib import font_manager
import pandas as pd
import numpy as np
import os
import sys
import logging

# 中文字体
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']



# 日志
logger = logging.getLogger("draw_diagram")
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
ch.setFormatter(
    logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
)
logger.addHandler(ch)


# 数据源文件
source_file = sys.argv[1]
print(source_file)


# Read the source data
df1 = pd.read_csv(source_file, sep=",")


# Convert columns to numeric
num_cols = [
    "cpus_load",
    "cpus_service",
    "concurrency",
    "lat_avg",
    "lat_stdev",
    "lat_max",
    "req_avg",
    "req_stdev",
    "req_max",
    "tot_requests",
    "tot_duration",
    "read",
    "err_connect",
    "err_read",
    "err_write",
    "err_timeout",
    "req_sec_tot",
    "read_tot",
    "user_cpu",
    "kern_cpu",
    "mem_kb_uss",
    "mem_kb_pss",
    "mem_kb_rss",
    "duration",
]
for col in num_cols:
    df1[col] = pd.to_numeric(df1[col])
    df1["total_cpu"] = df1["user_cpu"] + df1["kern_cpu"]
    df1["cpu_per_request"] = df1["total_cpu"] / df1["tot_requests"]
    df1["memory_per_request"] = df1["mem_kb_uss"] / df1["tot_requests"]


print(df1)
# Add avg and sem
grouped_df = (
    df1.groupby(
        [
            "description",
            "driver",
            "asyncservice",
            "pool_used",
            "asyncdriver",
            "servlet_engine",
            "framework",
            # "cpus_load",
            # "cpus_service",
            "concurrency",
        ]
    )
    .agg(["mean", "min", "max", "sem", "std"])
    .reset_index()
)

print(grouped_df)

grouped_df.columns = ["_".join(col).strip() for col in grouped_df.columns.values]

print("================== grouped_df.columns ==================")
print(grouped_df.columns)

grouped_df = grouped_df.reset_index()

# Removing a single outlier
# i = grouped_df.loc[(grouped_df["tot_requests_min"] < 1500000) & (grouped_df["concurrency_"] == 150)].index
# grouped_df = grouped_df.drop(grouped_df.index[i])



def custom_plot(x, y, ymin, ymax, **kwargs):
    ax = kwargs.pop("ax", plt.gca())
    (base_line,) = ax.plot(x, y, **kwargs)
    ax.fill_between(x, ymin, ymax, facecolor=base_line.get_color(), alpha=0.2)


if sys.argv[2] == '1':
    plt.close("all")
    figure(num=None, figsize=(10, 8))
    for group_name in grouped_df["description_"].unique():
        plot_data = grouped_df.loc[grouped_df["description_"] == group_name]
        df_plot = pd.DataFrame(
            {
                "x": plot_data["concurrency_"],
                "y": plot_data["lat_avg_mean"],
                "ymin": plot_data["lat_avg_mean"] - plot_data["lat_avg_std"],
                "ymax": plot_data["lat_avg_mean"] + plot_data["lat_avg_std"],
            }
        )
        custom_plot(
            df_plot["x"],
            df_plot["y"],
            df_plot["ymin"],
            df_plot["ymax"],
            label=group_name,
        )

    plt.title("延迟率-Latency")
    plt.ylabel("平均响应时间 [ms]")
    plt.xlabel("并发线程数 [个]")
    plt.grid(True)
    plt.legend()
    plt.savefig('Pic-延迟率.png', bbox_inches='tight')
    print("============================ 延迟率 finished！ ============================")


if sys.argv[2] == '2':
    plt.close("all")
    figure(num=None, figsize=(10, 8))
    for group_name in grouped_df["description_"].unique():
        plot_data = grouped_df.loc[grouped_df["description_"] == group_name]
        df_plot = pd.DataFrame(
            {
                "x": plot_data["concurrency_"],
                "y": plot_data["tot_requests_mean"],
                "ymin": plot_data["tot_requests_mean"] - plot_data["tot_requests_std"],
                "ymax": plot_data["tot_requests_mean"] + plot_data["tot_requests_std"],
            }
        )
        custom_plot(
            df_plot["x"],
            df_plot["y"],
            df_plot["ymin"],
            df_plot["ymax"],
            label=group_name,
        )

    plt.title("吞吐量-Throughout")
    plt.ylabel("120s内处理的请求数 [个]")
    plt.xlabel("并发线程数 [个]")
    plt.grid(True)
    plt.legend()
    plt.savefig('Pic-吞吐量.png', bbox_inches='tight')
    print("============================ 吞吐量 finished！ ============================")


if sys.argv[2] == '3':
    plt.close("all")
    figure(num=None, figsize=(10, 8))
    for group_name in grouped_df["description_"].unique():
        plot_data = grouped_df.loc[grouped_df["description_"] == group_name]
        df_plot = pd.DataFrame(
            {
                "x": plot_data["concurrency_"],
                "y": plot_data["cpu_per_request_mean"],
                "ymin": plot_data["cpu_per_request_mean"] - plot_data["cpu_per_request_std"],
                "ymax": plot_data["cpu_per_request_mean"] + plot_data["cpu_per_request_std"],
            }
        )
        custom_plot(
            df_plot["x"],
            df_plot["y"],
            df_plot["ymin"],
            df_plot["ymax"],
            label=group_name,
        )

    plt.title("单个请求的CPU平均占用率")
    plt.ylabel("CPU的使用时长 (user + kernel) [时钟周期] / 处理的请求数 [个]")
    plt.xlabel("并发线程数 [个]")
    plt.grid(True)
    plt.legend()
    plt.savefig('Pic-CPU使用率.png', bbox_inches='tight')
    print("============================ CPU使用率 finished！ ============================")


if sys.argv[2] == '4':
    plt.close("all")
    figure(num=None, figsize=(10, 8))
    for group_name in grouped_df["description_"].unique():
        plot_data = grouped_df.loc[grouped_df["description_"] == group_name]
        df_plot = pd.DataFrame(
            {
                "x": plot_data["concurrency_"],
                "y": plot_data["mem_kb_pss_mean"] / 8 / 1000,
                "ymin": (plot_data["mem_kb_pss_mean"] - plot_data["mem_kb_pss_std"]) / 8 / 1000,
                "ymax": (plot_data["mem_kb_pss_mean"] + plot_data["mem_kb_pss_std"]) / 8 / 1000,
            }
        )
        custom_plot(
            df_plot["x"],
            df_plot["y"],
            df_plot["ymin"],
            df_plot["ymax"],
            label=group_name,
        )

    plt.title("内存占用率")
    plt.ylabel("java进程占用的内存大小 [MB]")
    plt.xlabel("并发线程数 [个]")
    plt.grid(True)
    plt.legend()
    plt.savefig('Pic-内存占用率.png', bbox_inches='tight')
    print("============================ 内存占用率 finished！ ============================")
