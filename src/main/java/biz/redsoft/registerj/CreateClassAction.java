package biz.redsoft.registerj;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;

public class CreateClassAction implements BulkFileListener {
  private final MessageBusConnection connection;

  public CreateClassAction() {
    connection = ApplicationManager.getApplication().getMessageBus().connect();
  }

  public void initComponent() {
    connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
  }

  public void disposeComponent() {
    connection.disconnect();
  }

  @Override
  public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
    initComponent();
    for (VFileEvent e : events) {
      if (e instanceof VFileCreateEvent) {
        VFileCreateEvent event = (VFileCreateEvent) e;
        if (event.getFile() != null && event.getFile().getName().contains("Object")) {
          writeTemplate(event.getFile());
        }
      }
    }
    disposeComponent();
    BulkFileListener.super.after(events);
  }

  private void writeTemplate(VirtualFile file){
    try {
      byte [] template = getTemplate(file.getName()).getBytes();
      file.setBinaryContent(template);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getTemplate(String fileName){
    VelocityEngine velocityEngine = new VelocityEngine();
    velocityEngine.init();

    Template t = velocityEngine.getTemplate("class.vm");

    VelocityContext context = new VelocityContext();
    context.put("NAME", fileName);

    StringWriter writer = new StringWriter();
    t.merge( context, writer );
    return writer.toString();
  }
}
