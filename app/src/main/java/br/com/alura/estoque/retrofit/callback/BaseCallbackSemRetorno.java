package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class BaseCallbackSemRetorno extends BaseCallback<Void>{

    public BaseCallbackSemRetorno(RespostaCallback<Void> callback) {
        super(callback);
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<Void> call, Response<Void> response) {
        if (response.isSuccessful()) {
                this.callback.quandoSucesso(null);
        }
    }
}
