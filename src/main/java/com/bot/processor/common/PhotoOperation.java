package com.bot.processor.common;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.model.Operations;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class PhotoOperation {

    public static MessageWrapper addPhoto(User user, Update update, TempObject tempObject, Operations nextOperation) {
        String text;
        if (user.isWaitingMessages()) {
            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            for (PhotoSize photoSize : photoSizes) {
                //76 символов уникальные без учета разрешения
                String id = photoSize.getFileId().substring(0, 75);

                if (!containsPhoto(tempObject.getSelectedData().getPhotos(),id)) {
                    tempObject.getSelectedData().getPhotos().add(photoSize.getFileId());
                }
            }

            String keyForPhotoElse = Util.generateToken(tempObject);
            user.setLastCallback(keyForPhotoElse);

            String buttonName = "Готово";
            text = "Отлично. Если есть еще фото - дай их мне. Если нет - нажми кнопку " + buttonName;
            List<ButtonWrapper> data = new ArrayList<>();
            TempObject newTemp = tempObject.clone();
            newTemp.setOperation(nextOperation);
            data.add(new ButtonWrapper(buttonName, Util.generateToken(newTemp), newTemp));
            return ProcessorUtil.createMessages(text, update, data).addTemp(keyForPhotoElse, tempObject);
        } else {
            text = "Пришли мне фотографии. Фото можно прислать как по одному, так и группой.";
            user.setWaitingMessages(true);
            String key = Util.generateToken(tempObject);
            user.setLastCallback(key);
            MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update);
            messageWrapper.addTemp(key, tempObject);
            return messageWrapper;
        }
    }

    private static boolean containsPhoto(List<String> photos, String newChunkId) {

        for (String id : photos) {
            if (id.contains(newChunkId)) {
                return true;
            }
        }
        return false;
    }
}
