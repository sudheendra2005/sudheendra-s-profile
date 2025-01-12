import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class PomodoroTimer extends JFrame {
    private JLabel timeLabel;
    private JButton startButton, stopButton, resetButton;
    private JButton addTaskButton;
    private JList<Task> taskList;
    private DefaultListModel<Task> listModel;
    private Timer timer;
    private int timeLeft;
    private boolean isRunning;
    private final int WORK_TIME = 25 * 60; // 25 minutes in seconds
    private final int BREAK_TIME = 5 * 60; // 5 minutes in seconds
    private boolean isWorkTime = true;

    public PomodoroTimer() {
        setTitle("Pomodoro Timer with Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLayout(new BorderLayout());

        // Initialize components
        initializeComponents();
        
        // Layout setup
        setupLayout();

        timeLeft = WORK_TIME;
        updateTimeLabel();
        isRunning = false;
    }

    @SuppressWarnings("unused")
    private void initializeComponents() {
        // Time display
        timeLabel = new JLabel("25:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));

        // Control buttons
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");
        addTaskButton = new JButton("Add Task");

        // Task list
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add mouse listener for task completion toggle
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Double click
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Task task = listModel.getElementAt(index);
                        task.toggleComplete();
                        taskList.repaint();
                    }
                }
            }
        });

        // Add keyboard listener for delete
        taskList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {
                    deleteSelectedTask();
                }
            }
        });

        // Add action listeners for buttons - removed unused lambda parameters
        startButton.addActionListener(__ -> startTimer());
        stopButton.addActionListener(__ -> stopTimer());
        resetButton.addActionListener(__ -> resetTimer());
        addTaskButton.addActionListener(__ -> addNewTask());
    }

    private void deleteSelectedTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
        }
    }

    private void setupLayout() {
        // Timer panel
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.add(timeLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);

        // Combine timer and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(timerPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Task panel with instructions
        JPanel taskPanel = new JPanel(new BorderLayout());
        JLabel instructionsLabel = new JLabel(
            "<html><center>Double-click to toggle task completion<br>Press Delete to remove task</center></html>",
            SwingConstants.CENTER
        );
        taskPanel.add(instructionsLabel, BorderLayout.NORTH);
        taskPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        taskPanel.add(addTaskButton, BorderLayout.SOUTH);

        // Add to frame
        add(topPanel, BorderLayout.NORTH);
        add(taskPanel, BorderLayout.CENTER);
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (timeLeft > 0) {
                        timeLeft--;
                        SwingUtilities.invokeLater(() -> updateTimeLabel());
                    } else {
                        timer.cancel();
                        isRunning = false;
                        SwingUtilities.invokeLater(() -> switchMode());
                    }
                }
            }, 1000, 1000);
        }
    }

    private void stopTimer() {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
        }
    }

    private void resetTimer() {
        stopTimer();
        timeLeft = isWorkTime ? WORK_TIME : BREAK_TIME;
        updateTimeLabel();
    }

    private void switchMode() {
        isWorkTime = !isWorkTime;
        timeLeft = isWorkTime ? WORK_TIME : BREAK_TIME;
        updateTimeLabel();
        String message = isWorkTime ? "Work Time!" : "Break Time!";
        Toolkit.getDefaultToolkit().beep();  // Add sound notification
        JOptionPane.showMessageDialog(this, message);
    }

    private void updateTimeLabel() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void addNewTask() {
        String taskName = JOptionPane.showInputDialog(this, "Enter task name:");
        if (taskName != null && !taskName.trim().isEmpty()) {
            Task newTask = new Task(taskName);
            listModel.addElement(newTask);
        }
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new PomodoroTimer().setVisible(true);
        });
    }
}

class Task {
    private String name;
    private boolean completed;

    public Task(String name) {
        this.name = name;
        this.completed = false;
    }

    public void toggleComplete() {
        completed = !completed;
    }

    @Override
    public String toString() {
        return (completed ? "✅ " : "⬜ ") + name;
    }
}