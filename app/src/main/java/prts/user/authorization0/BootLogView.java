package prts.user.authorization0;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.util.Log;
import android.content.Intent;

public class BootLogView extends View {
    private Paint textPaint;
    private List<String> logLines;
    private Handler handler;
    private Random random;
    private int currentLine = 0;
    private boolean showCursor = false;
    private boolean bootCompleted = false;
    private BootCompleteListener bootCompleteListener;

    // 滚动相关变量
    private int scrollOffset = 0;
    private int maxVisibleLines = 0;
    private int lineHeight = 30;
    private int textSize = 24;

    // 模拟的启动日志内容 - 重新排序
    private String[] bootSequence = {
        "[    0.000000] Initializing cgroup subsys cpuset",
        "[    0.000000] Initializing cgroup subsys cpu",
        "[    0.000000] Initializing cgroup subsys cpuacct",
        "[    0.000000] Linux version 4.15.0-ubuntu (buildd@lcy02-amd64-023)",
        "[    0.000000] Command line: BOOT_IMAGE=/boot/vmlinuz-4.15.0-ubuntu root=UUID=xxxx ro quiet splash",
        "[    0.000000] KERNEL supported cpus:",
        "[    0.000000]   Intel GenuineIntel",
        "[    0.000000]   AMD AuthenticAMD",
        "[    0.000000]   Centaur CentaurHauls",
        "[    0.000000] x86/fpu: Supporting XSAVE feature 0x001: 'x87 floating point registers'",
        "[    0.000000] x86/fpu: Supporting XSAVE feature 0x002: 'SSE registers'",
        "[    0.100000] e820: BIOS-provided physical RAM map:",
        "[    0.100000] BIOS-e820: [mem 0x0000000000000000-0x000000000009fbff] usable",
        "[    0.100000] BIOS-e820: [mem 0x000000000009fc00-0x000000000009ffff] reserved",
        "[    0.200000] SMBIOS 2.8 present.",
        "[    0.300000] DMI: innotek GmbH VirtualBox/VirtualBox, BIOS VirtualBox 12/01/2006",
        "[    0.400000] Hypervisor detected: KVM",
        "[    0.500000] tsc: Fast TSC calibration using PIT",
        "[    0.600000] CPU MTRRs all blank - virtualized system.",
        "[    0.700000] ACPI: Early table checksum verification disabled",
        "[    0.800000] ACPI: RSDP 0x00000000000E0000 000024 (v02 VBOX  )",
        "[    0.900000] Mount-cache hash table entries: 2048 (order: 1, 8192 bytes)",
        "[    1.000000] Mountpoint-cache hash table entries: 2048 (order: 1, 8192 bytes)",
        "[    1.100000] devtmpfs: initialized",
        "[    1.200000] clocksource: jiffies: mask: 0xffffffff max_cycles: 0xffffffff, max_idle_ns: 19112604462750000 ns",
        "[    1.300000] pci_bus 0000:00: root bus resource [io  0x0000-0x03ff]",
        "[    1.400000] NET: Registered protocol family 16",
        "[    1.500000] DMA: preallocated 2048 KiB pool for atomic allocations",
        "[    1.600000] audit: initializing netlink subsys (disabled)",
        "[    1.700000] cpuidle: using governor ladder",
        "[    1.800000] Simple Boot Flag at 0x37 set to 0x1",
        "[    1.900000] PCI: Using configuration type 1 for base access",
        "[    2.500000] HugeTLB registered 2.00 MiB page size, pre-allocated 0 pages",
        "[    2.600000] ACPI: Added _OSI(Module Device)",
        "[    2.700000] ACPI: Added _OSI(Processor Device)",
        "[    2.800000] ACPI: Added _OSI(3.0 _SCP Extensions)",
        "[    2.900000] ACPI: Added _OSI(Processor Aggregator Device)",
        "[    3.000000] ACPI: Interpreter enabled",
        "[    3.100000] ACPI: (supports S0 S5)",
        "[    3.200000] ACPI: Using IOAPIC for interrupt routing",
        "[    3.300000] PCI: Using host bridge windows from ACPI; if necessary, use \"pci=nocrs\" and report a bug",
        "[    3.400000] ACPI: PCI Root Bridge [PCI0] (domain 0000 [bus 00-ff])",
        "[    3.500000] ACPI: PCI Interrupt Link [LNKA] (IRQs 3 4 5 6 10 11 12 14 15) *0",
        "[    3.600000] ACPI: PCI Interrupt Link [LNKB] (IRQs 3 4 5 6 10 11 12 14 15) *0",
        "[    3.700000] ACPI: PCI Interrupt Link [LNKC] (IRQs 3 4 5 6 10 11 12 14 15) *0",
        "[    3.800000] ACPI: PCI Interrupt Link [LNKD] (IRQs 3 4 5 6 10 11 12 14 15) *0",
        "[    3.900000] ACPI: PCI Interrupt Link [LNKS] (IRQs 3 4 5 6 10 11 12 14 15) *0",
        "[    4.000000] iommu: Default domain type: Translated",
        "[    4.100000] SCSI subsystem initialized",
        "[    4.200000] libata version 3.00 loaded.",
        "[    4.300000] ACPI: bus type USB registered",
        "[    4.400000] usbcore: registered new interface driver usbfs",
        "[    4.500000] usbcore: registered new interface driver hub",
        "[    4.600000] usbcore: registered new device driver usb",
        "[    4.700000] pps_core: LinuxPPS API ver. 1 registered",
        "[    4.800000] pps_core: Software ver. 5.3.6 - Copyright 2005-2007 Rodolfo Giometti <giometti@linux.it>",
        "[    4.900000] PTP clock support registered",
        "[    5.000000] clocksource: Switched to clocksource kvm-clock",
        "[    5.100000] NTP Service Started",
        "[    5.200000] Closure Patcher is running!",
        "[    5.300000] ALL CHECKED.",
        "            ",
        "",
        "",
        "",
        "",
        "",
        "[    6.000000] Startup Rhodes Island Authorization"
        
    };

    public void setBootCompleteListener(SecondaryStartup p0) {
    }

    public void setBootCompleteListener(SplashActivity p0) {
    }

    public interface BootCompleteListener {
        void onBootComplete();
    }

    public BootLogView(Context context) {
        super(context);
        init();
    }

    public BootLogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setBootCompleteListener(BootCompleteListener listener) {
        this.bootCompleteListener = listener;
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setAntiAlias(true);

        logLines = new ArrayList<>();
        handler = new Handler();
        random = new Random();

        // 设置黑色背景
        setBackgroundColor(Color.BLACK);

        // 开始模拟启动过程
        startBootSequence();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算屏幕可以显示的最大行数
        maxVisibleLines = h / lineHeight;
    }

    private void startBootSequence() {
        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentLine < bootSequence.length) {
                        // 显示普通系统日志
                        logLines.add(bootSequence[currentLine]);
                        currentLine++;

                        // 自动滚动到底部
                        autoScrollToBottom();
                        invalidate();

                        // 随机延迟，模拟真实启动过程
                        int delay = random.nextInt(80) + 40;
                        handler.postDelayed(this, delay);
                    } else if (!bootCompleted) {
                        // 所有系统日志显示完成后，按顺序显示特殊行
                        bootCompleted = true;
                        showSpecialLinesInOrder();
                    }
                }
            }, 1000);
    }

    private void autoScrollToBottom() {
        // 计算需要滚动的距离，让最后一行在屏幕底部
        int totalHeight = logLines.size() * lineHeight;
        int viewHeight = getHeight();

        if (totalHeight > viewHeight) {
            scrollOffset = totalHeight - viewHeight + lineHeight;
        } else {
            scrollOffset = 0;
        }
    }

    private void showSpecialLinesInOrder() {
        // 第一步：显示 su
        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logLines.add("+ exec su");
                    autoScrollToBottom();
                    invalidate();

                    // 第二步：1秒后显示 tty
                    handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                logLines.add("You are in /dev/tty1");
                                autoScrollToBottom();
                                invalidate();

                                // 第三步：1秒后显示初始化信息
                                handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            logLines.add("System Initiallzing...");
                                            autoScrollToBottom();
                                            invalidate();

                                            // 开始光标闪烁
                                            startCursorBlink();

                                            // 第四步：等待8秒后进入主界面
                                            handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.d("BootLogView", "Boot completed, calling listener");
                                                        if (bootCompleteListener != null) {
                                                            bootCompleteListener.onBootComplete();
                                                            onStartService();
                                                        } else {
                                                            Log.e("BootLogView", "bootCompleteListener is null!");
                                                            
                                                        }
                                                    }

                                                    private void onStartService() {
                                                    }
                                                }, 3000); // 等待3秒
                                        }
                                    }, 400); // 初始化信息延迟0.4秒
                            }
                        }, 800); // tty延迟0.8秒
                }
            }, 500); // su延迟0.5秒
            
    }

    // 添加一个公共方法用于手动触发跳转（用于测试）
    public void triggerBootComplete() {
        if (bootCompleteListener != null) {
            bootCompleteListener.onBootComplete();
        }
    }
    

    private void startCursorBlink() {
        showCursor = true;
        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (showCursor) {
                        showCursor = !showCursor;
                        invalidate();
                        handler.postDelayed(this, 500); // 每500毫秒闪烁一次
                    }
                }
            }, 500);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = 20;
        int startY = 40 - scrollOffset; // 应用滚动偏移
        int currentY = startY;

        // 绘制所有日志行
        for (int i = 0; i < logLines.size(); i++) {
            String line = logLines.get(i);

            // 只绘制在可见区域内的行
            if (currentY + lineHeight >= 0 && currentY <= getHeight()) {
                // 设置颜色
                if (line.equals("You are in /dev/tty1")) {
                    textPaint.setColor(Color.GREEN);
                } else if (line.equals("System Initiallzing...") || line.equals("+ exec su")) {
                    textPaint.setColor(Color.WHITE);
                } else {
                    // 系统日志根据内容设置不同颜色
                    if (line.contains("error") || line.contains("fail")) {
                        textPaint.setColor(Color.RED);
                    } else if (line.contains("warn")) {
                        textPaint.setColor(Color.YELLOW);
                    } else {
                        textPaint.setColor(Color.rgb(180, 180, 180)); // 浅灰色
                    }
                }

                canvas.drawText(line, x, currentY, textPaint);
            }

            currentY += lineHeight;
        }

        // 绘制闪烁的光标（在可见区域内才绘制）
        if (showCursor && currentY >= 0 && currentY <= getHeight()) {
            textPaint.setColor(Color.WHITE);
            canvas.drawText("_", x, currentY, textPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
