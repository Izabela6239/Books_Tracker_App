package com.example.bookstracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class ReadingReminderWorker extends Worker {

    public ReadingReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        BookDao bookDao = BookDatabase.getInstance(getApplicationContext()).bookDao();

        List<Book> allBooks = bookDao.getAllBooks();

        boolean hasBooksInProgress = false;

        for (Book book : allBooks) {
            if ("In curs".equals(book.getStatus())) {
                hasBooksInProgress = true;

                int currentStreak = book.getStreak();
                book.setStreak(currentStreak + 1);

                bookDao.update(book);
            }
        }

        if (hasBooksInProgress) {
            sendNotification("Continuă să citești!", "Streak-ul tău a crescut! Nu uita să citești și azi.");
        }

        return Result.success();
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reading_reminder";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.outline_auto_stories_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Notificarea dispare când dai click pe ea

        notificationManager.notify(1, builder.build());
    }
}