var sCmds = newMenu("Easy Menu Tool",
newArray("RGB Color","Color Picker...", "-", "Hide Overlay", "Show Overlay",  "To ROI Manager","-", "About"));

 macro "Easy Menu Tool -  C000D11D12D13D14D15D16D17D18D19D1aD1bD1cD1dD1eD21D22D23D24D25D26D27D28D29D2aD2bD2cD2dD2eD31D32D37D38D3dD3eD41D42D47D48D4dD4eD51D52D57D58D5eD61D67D68D6eD71D77D78D7eD81D87D88D8eD91D97D98D9eDa1Da2Da7Da8DaeDb1Db2Db7Db8DbeDc1Dc2Dc7Dc8DceDd1Dd2Dd7Dd8DddDdeDe1De2DedDeeC800C080C880C008C808C088CcccD00D04D05D0bD0dD0fD10D1fD2fD33D34D35D3aD3cD40D44D45D46D4aD4bD54D55D5aD5bD64D65D66D6aD6bD74D75D76D7aD7bD84D85D8aD8bD94D95D9aD9bDa4Da5DaaDabDb4Db5DbaDbbDc4Dc5Dc9DcaDcbDd0Dd4Dd5Dd9DdaDdbDdfDe4De5De6De9DeaDebDf0Df1Df3Df4Df5Df6Df7Df8Df9DfaDfbDfcDfeDffCcdcD03D06D07D08D09D0aD0cD30D3bD50D56D59D60D69D70D79D80D89D90D99Da0Da6Da9Db0Db6Db9Dc0CacfC420C620C820Ca20Cc20Ce20C040C240C440C640C840Ca40Cc40Ce40C060C260C460C660C860Ca60Cc60Ce60C080C280C480C680C880Ca80Cc80Ce80C0a0C2a0C4a0C6a0C8a0Caa0Cca0Cea0C0c0C2c0C4c0C6c0C8c0Cac0Ccc0Cec0C0e0C2e0C4e0C6e0C8e0Cae0Cce0Cee0C004C204C404C604C804Ca04Cc04Ce04C024C224D5dD62D6dD72D7dD82D8dD92D9dDadDbdDcdC424C624C824Ca24Cc24Ce24C044C244C444C644C844Ca44Cc44Ce44C064C264C464C664C864Ca64Cc64Ce64C084C284C484C684C884Ca84Cc84Ce84C0a4C2a4C4a4C6a4C8a4Caa4Cca4Cea4C0c4C2c4C4c4C6c4C8c4Cac4Ccc4Cec4C0e4C2e4C4e4C6e4C8e4Cae4Cce4Cee4C008C208C408C608C808Ca08Cc08Ce08C028C228C428C628C828Ca28Cc28Ce28C048C248C448C648C848Ca48Cc48Ce48C068C268C468C668C868Ca68Cc68Ce68C088C288C488C688C888D4fD5fD6fD7fD8fD9fDafDbfCa88Cc88Ce88C0a8C2a8C4a8C6a8C8a8DcfDd3Caa8Cca8Cea8C0c8C2c8C4c8C6c8C8c8Cac8Ccc8Cec8C0e8C2e8C4e8C6e8C8e8Cae8Cce8Cee8C00cC20cC40cC60cC80cCa0cCc0cCe0cC02cC22cC42cC62cC82cCa2cCc2cCe2cC04cC24cC44cC64cC84cCa4cCc4cCe4cC06cC26cC46cC66cC86cCa6cCc6cCe6cC08cC28cC48cC68cC88cCa8cCc8cCe8cC0acC2acC4acC6acC8acCaacD39DefCcacCeacC0ccC2ccC4ccC6ccC8ccCaccD01D02D0eD20D36D53D63D73D83D96Dc6DdcDe0De3De7De8Df2DfdCfffCaaaD3fD43D49D4cD5cD6cD7cD86D8cD93D9cDa3DacDb3DbcDc3DccDd6Dec" {
 cmd = getArgument();
    if (cmd=="About") {
     print ("Makes Life Easier");
    exit;
 }
    if (cmd!="-") run(cmd);
 } 
